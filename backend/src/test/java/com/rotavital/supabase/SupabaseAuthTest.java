package com.rotavital.supabase;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotavital.servico.AutenticacaoIndisponivelException;
import com.rotavital.servico.SessaoAutenticada;

// Conversa com o Supabase Auth sem rede: o MockRestServiceServer responde no lugar dele,
// com os corpos que a API de lá devolve.
public class SupabaseAuthTest
{
    private static final String URL_TOKEN = "https://projeto.supabase.co/auth/v1/token?grant_type=password";

    private final ObjectMapper json = new ObjectMapper();
    private MockRestServiceServer supabase;
    private SupabaseAuth auth;

    @BeforeEach
    public void ligarNoServidorFalso()
    {
        RestClient.Builder builder = RestClient.builder();
        supabase = MockRestServiceServer.bindTo(builder).build();
        auth = SupabaseAuth.semTempoLimite("https://projeto.supabase.co/", "chave-publica", builder);
    }

    @Test
    public void credenciaisCertasDevolvemASessao()
    {
        supabase.expect(paraOToken())
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(pedido -> Assertions.assertEquals("chave-publica", pedido.getHeaders().getFirst("apikey")))
                .andExpect(pedido -> Assertions.assertEquals(
                        json.readTree("{\"email\": \"ana@rotavital.dev\", \"password\": \"certa\"}"),
                        json.readTree(((MockClientHttpRequest) pedido).getBodyAsString())))
                .andRespond(MockRestResponseCreators.withSuccess("""
                        {"access_token": "eyJ...", "token_type": "bearer", "expires_in": 3600,
                         "refresh_token": "r1", "user": {"id": "6f1c0a52-8a3e-4c39-9d6a-1b2f3c4d5e6f",
                         "email": "ana@rotavital.dev", "role": "authenticated"}}
                        """, MediaType.APPLICATION_JSON));

        Optional<SessaoAutenticada> sessao = auth.verificar("ana@rotavital.dev", "certa");

        Assertions.assertEquals(Optional.of(new SessaoAutenticada(
                UUID.fromString("6f1c0a52-8a3e-4c39-9d6a-1b2f3c4d5e6f"), "eyJ...", 3600)), sessao);
        supabase.verify();
    }

    @Test
    public void credenciaisErradasDevolvemVazio()
    {
        supabase.expect(paraOToken())
                .andRespond(MockRestResponseCreators.withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": 400, \"error_code\": \"invalid_credentials\", \"msg\": \"Invalid login credentials\"}"));

        Assertions.assertEquals(Optional.empty(), auth.verificar("ana@rotavital.dev", "errada"));
    }

    @Test
    public void erroDoSupabaseViraIndisponivel()
    {
        supabase.expect(paraOToken())
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(AutenticacaoIndisponivelException.class,
                () -> auth.verificar("ana@rotavital.dev", "certa"));
    }

    @Test
    public void semChaveConfiguradaNemChamaOSupabase()
    {
        SupabaseAuth semChave = SupabaseAuth.semTempoLimite("https://projeto.supabase.co", "", RestClient.builder());

        AutenticacaoIndisponivelException erro = Assertions.assertThrows(AutenticacaoIndisponivelException.class,
                () -> semChave.verificar("ana@rotavital.dev", "certa"));

        Assertions.assertTrue(erro.getMessage().contains("SUPABASE_KEY"));
    }

    // O projeto não traz o Hamcrest, então a URL é conferida aqui em vez de MockRestRequestMatchers.requestTo.
    private static RequestMatcher paraOToken()
    {
        return pedido -> Assertions.assertEquals(URL_TOKEN, pedido.getURI().toString());
    }
}
