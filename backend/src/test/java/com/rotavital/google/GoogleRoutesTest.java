package com.rotavital.google;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotavital.dominio.Coordenada;
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.Trajeto;

// Conversa com a Routes API do Google sem rede (PI3-262): o MockRestServiceServer responde no lugar
// dele, com os corpos que o computeRoutes devolve.
public class GoogleRoutesTest
{
    private static final String URL = "https://routes.googleapis.com/directions/v2:computeRoutes";
    private static final Coordenada DOADOR = new Coordenada(-8.0578, -34.8829);
    private static final Coordenada HOSPITAL = new Coordenada(-8.0512, -34.9478);

    private final ObjectMapper json = new ObjectMapper();
    private MockRestServiceServer google;
    private GoogleRoutes routes;

    @BeforeEach
    public void ligarNoServidorFalso()
    {
        RestClient.Builder builder = RestClient.builder();
        google = MockRestServiceServer.bindTo(builder).build();
        routes = GoogleRoutes.semTempoLimite("https://routes.googleapis.com/", "chave-do-google", builder);
    }

    @Test
    public void pedeARotaDeCarroComTransitoEDevolveDistanciaETempo()
    {
        google.expect(MockRestRequestMatchers.requestTo(URL))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(MockRestRequestMatchers.header("X-Goog-Api-Key", "chave-do-google"))
                .andExpect(MockRestRequestMatchers.header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration"))
                .andExpect(pedido -> Assertions.assertEquals(json.readTree("""
                        {"origin": {"location": {"latLng": {"latitude": -8.0578, "longitude": -34.8829}}},
                         "destination": {"location": {"latLng": {"latitude": -8.0512, "longitude": -34.9478}}},
                         "travelMode": "DRIVE", "routingPreference": "TRAFFIC_AWARE",
                         "languageCode": "pt-BR", "units": "METRIC"}
                        """), json.readTree(((MockClientHttpRequest) pedido).getBodyAsString())))
                .andRespond(MockRestResponseCreators.withSuccess("""
                        {"routes": [{"distanceMeters": 9412, "duration": "1094s"}]}
                        """, MediaType.APPLICATION_JSON));

        Optional<Trajeto> trajeto = routes.calcular(DOADOR, HOSPITAL);

        // 1094 s = 18,2 min: arredonda para 19.
        Assertions.assertEquals(Optional.of(new Trajeto(9.412, 19, FonteDoTrajeto.GOOGLE_ROUTES)), trajeto);
        google.verify();
    }

    @Test
    public void pedeOTracadoPassandoPelasParadasDoCaminho()
    {
        Coordenada parada = new Coordenada(-8.0432, -34.9331);

        google.expect(MockRestRequestMatchers.requestTo(URL))
                .andExpect(MockRestRequestMatchers.header("X-Goog-FieldMask",
                        "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline"))
                .andExpect(pedido -> Assertions.assertEquals(json.readTree("""
                        {"origin": {"location": {"latLng": {"latitude": -8.0578, "longitude": -34.8829}}},
                         "destination": {"location": {"latLng": {"latitude": -8.0512, "longitude": -34.9478}}},
                         "intermediates": [{"location": {"latLng": {"latitude": -8.0432, "longitude": -34.9331}}}],
                         "travelMode": "DRIVE", "routingPreference": "TRAFFIC_AWARE",
                         "languageCode": "pt-BR", "units": "METRIC"}
                        """), json.readTree(((MockClientHttpRequest) pedido).getBodyAsString())))
                .andRespond(MockRestResponseCreators.withSuccess("""
                        {"routes": [{"distanceMeters": 9412, "duration": "1094s",
                                     "polyline": {"encodedPolyline": "abc~d@"}}]}
                        """, MediaType.APPLICATION_JSON));

        Optional<Trajeto> trajeto = routes.calcularComTracado(List.of(DOADOR, parada, HOSPITAL));

        Assertions.assertEquals(Optional.of(new Trajeto(9.412, 19, FonteDoTrajeto.GOOGLE_ROUTES, "abc~d@")), trajeto);
        google.verify();
    }

    @Test
    public void semCaminhoDeCarroDevolveVazio()
    {
        google.expect(MockRestRequestMatchers.requestTo(URL))
                .andRespond(MockRestResponseCreators.withSuccess("{}", MediaType.APPLICATION_JSON));

        Assertions.assertEquals(Optional.empty(), routes.calcular(DOADOR, HOSPITAL));
    }

    @Test
    public void origemIgualAoDestinoVemSemDistanciaNoJson()
    {
        // O JSON do Google omite campos zerados: distanceMeters some.
        google.expect(MockRestRequestMatchers.requestTo(URL))
                .andRespond(MockRestResponseCreators.withSuccess("{\"routes\": [{\"duration\": \"0s\"}]}",
                        MediaType.APPLICATION_JSON));

        Assertions.assertEquals(Optional.of(new Trajeto(0, 0, FonteDoTrajeto.GOOGLE_ROUTES)),
                routes.calcular(HOSPITAL, HOSPITAL));
    }

    @Test
    public void chaveRecusadaPeloGoogleDevolveVazio()
    {
        google.expect(MockRestRequestMatchers.requestTo(URL))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"error": {"code": 403, "status": "PERMISSION_DENIED",
                                 "message": "Routes API has not been used in project 123 before or it is disabled."}}
                                """));

        Assertions.assertEquals(Optional.empty(), routes.calcular(DOADOR, HOSPITAL));
    }

    @Test
    public void erroDoGoogleDevolveVazio()
    {
        google.expect(MockRestRequestMatchers.requestTo(URL))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        Assertions.assertEquals(Optional.empty(), routes.calcular(DOADOR, HOSPITAL));
    }

    @Test
    public void duracaoQueNaoEhNumeroDevolveVazio()
    {
        google.expect(MockRestRequestMatchers.requestTo(URL))
                .andRespond(MockRestResponseCreators.withSuccess(
                        "{\"routes\": [{\"distanceMeters\": 100, \"duration\": \"abc\"}]}", MediaType.APPLICATION_JSON));

        Assertions.assertEquals(Optional.empty(), routes.calcular(DOADOR, HOSPITAL));
    }

    @Test
    public void semChaveConfiguradaNemChamaOGoogle()
    {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer semPedidos = MockRestServiceServer.bindTo(builder).build();
        GoogleRoutes semChave = GoogleRoutes.semTempoLimite("https://routes.googleapis.com", " ", builder);

        Assertions.assertEquals(Optional.empty(), semChave.calcular(DOADOR, HOSPITAL));
        semPedidos.verify();
    }

    @Test
    public void duracaoComFracaoDeSegundoArredondaParaCima()
    {
        Assertions.assertEquals(13, GoogleRoutes.segundos("12.5s"));
        Assertions.assertEquals(165, GoogleRoutes.segundos("165s"));
        Assertions.assertEquals(0, GoogleRoutes.segundos(null));
    }
}
