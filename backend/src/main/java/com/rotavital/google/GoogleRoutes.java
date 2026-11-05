package com.rotavital.google;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.rotavital.dominio.Coordenada;
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.Trajeto;
import com.rotavital.servico.CalculadoraDeTrajeto;

/**
 * PI3-262 — Distância e tempo de carro pelas ruas de verdade, pela Routes API do Google
 * (POST /directions/v2:computeRoutes), com o trânsito do momento (TRAFFIC_AWARE).
 *
 * A chave (GOOGLE_MAPS_API_KEY) fica só no backend: vai no header X-Goog-Api-Key e nunca chega
 * ao navegador. O X-Goog-FieldMask pede só distância e duração, que é o que a tela mostra e o
 * que a cobrança do Google considera.
 *
 * PI3-259 — A tela de Rede pede também o traçado (polyline) de um caminho com paradas, para
 * desenhar no mapa as ruas do caminho que o Dijkstra escolheu, sem expor a chave no navegador.
 *
 * Qualquer problema (chave ausente, API desligada no projeto, tempo esgotado, sem caminho)
 * devolve vazio e fica no log: o serviço usa a estimativa em linha reta no lugar. A posição do
 * doador não vai para o log.
 */

@Component
public class GoogleRoutes implements CalculadoraDeTrajeto
{
    static final String CAMINHO = "/directions/v2:computeRoutes";
    static final String CAMPOS = "routes.distanceMeters,routes.duration";
    static final String CAMPOS_COM_TRACADO = CAMPOS + ",routes.polyline.encodedPolyline";

    private static final Logger LOG = LoggerFactory.getLogger(GoogleRoutes.class);
    private static final Duration TEMPO_LIMITE = Duration.ofSeconds(4);

    private final RestClient cliente;
    private final boolean chaveConfigurada;

    @Autowired
    public GoogleRoutes(@Value("${google.routes.url:https://routes.googleapis.com}") String url,
            @Value("${google.maps.api-key:}") String chave, RestClient.Builder builder)
    {
        this(montarCliente(url, chave, builder.requestFactory(fabricaComTempoLimite())), estaPreenchida(chave));

        if (!chaveConfigurada)
        {
            LOG.info("GOOGLE_MAPS_API_KEY não foi configurada: a rota até o hospital usa a estimativa em linha reta.");
        }
    }

    private GoogleRoutes(RestClient cliente, boolean chaveConfigurada)
    {
        this.cliente = cliente;
        this.chaveConfigurada = chaveConfigurada;
    }

    static GoogleRoutes semTempoLimite(String url, String chave, RestClient.Builder builder)
    {
        return new GoogleRoutes(montarCliente(url, chave, builder), estaPreenchida(chave));
    }

    @Override
    public Optional<Trajeto> calcular(Coordenada origem, Coordenada destino)
    {
        return pedir(PedidoDeRota.deCarro(List.of(origem, destino)), CAMPOS);
    }

    @Override
    public Optional<Trajeto> calcularComTracado(List<Coordenada> paradas)
    {
        return pedir(PedidoDeRota.deCarro(paradas), CAMPOS_COM_TRACADO);
    }

    private Optional<Trajeto> pedir(PedidoDeRota pedido, String campos)
    {
        if (!chaveConfigurada)
        {
            return Optional.empty();
        }

        try
        {
            RespostaRotas resposta = cliente.post()
                    .uri(CAMINHO)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Goog-FieldMask", campos)
                    .body(pedido)
                    .retrieve()
                    .body(RespostaRotas.class);

            if (resposta == null || resposta.routes() == null || resposta.routes().isEmpty())
            {
                LOG.warn("As 'Routes' não encontraram o caminho de carro até o hospital.");
                return Optional.empty();
            }

            Rota rota = resposta.routes().get(0);
            long segundos = segundos(rota.duration());
            int minutos = (int) Math.ceil(segundos / 60.0);
            String tracado = rota.polyline() == null ? null : rota.polyline().encodedPolyline();
            return Optional.of(new Trajeto(rota.distanceMeters() / 1000.0, minutos, FonteDoTrajeto.GOOGLE_ROUTES, tracado));
        }
        catch (RestClientResponseException e)
        {
            LOG.error("Routes API respondeu {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return Optional.empty();
        }

        catch (RestClientException | IllegalArgumentException e)
        {
            LOG.warn("Routes API indisponível: {}", e.getMessage());
            return Optional.empty();
        }
    }

    static long segundos(String duracao)
    {
        if (duracao == null || duracao.isBlank())
        {
            return 0;
        }
        
        String numero = duracao.trim().replaceFirst("s$", "");
        return new BigDecimal(numero).setScale(0, RoundingMode.CEILING).longValueExact();
    }

    private static RestClient montarCliente(String url, String chave, RestClient.Builder builder)
    {
        return builder
            .baseUrl(url.replaceAll("/+$", ""))
            .defaultHeader("X-Goog-Api-Key", estaPreenchida(chave) ? chave : "")
            .build();
    }

    private static boolean estaPreenchida(String chave)
    {
        return chave != null && !chave.isBlank();
    }

    private static SimpleClientHttpRequestFactory fabricaComTempoLimite()
    {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(TEMPO_LIMITE);
        fabrica.setReadTimeout(TEMPO_LIMITE);
        return fabrica;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    record PedidoDeRota(Ponto origin, Ponto destination, List<Ponto> intermediates, String travelMode,
    String routingPreference,  String languageCode, String units)
    {
        // A primeira parada é a origem, a última é o destino e as do meio viram pontos de passagem.
        static PedidoDeRota deCarro(List<Coordenada> paradas)
        {
            List<Ponto> pontos = paradas.stream().map(Ponto::de).toList();
            return new PedidoDeRota(pontos.get(0), pontos.get(pontos.size() - 1),
                    pontos.subList(1, pontos.size() - 1), "DRIVE", "TRAFFIC_AWARE", "pt-BR", "METRIC");
        }
    }

    record Ponto(Local location)
    {
        static Ponto de(Coordenada coordenada)
        {
            return new Ponto(new Local(new LatLng(coordenada.latitude(), coordenada.longitude())));
        }
    }

    record Local(LatLng latLng)
    {}

    record LatLng(double latitude, double longitude)
    {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaRotas(List<Rota> routes)
    {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Rota(long distanceMeters, String duration, Polilinha polyline)
    {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Polilinha(String encodedPolyline)
    {}
}