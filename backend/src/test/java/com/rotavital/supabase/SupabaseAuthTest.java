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
import com.rotavital.servico.ContaCriada;
import com.rotavital.servico.DadosInvalidosException;
import com.rotavital.servico.SessaoAutenticada;

// Conversa com o Supabase Auth sem rede: o MockRestServiceServer responde no lugar dele,
// com os corpos que a API de lá devolve.
public class SupabaseAuthTest
{
    private static final String URL_TOKEN = "https://projeto.supabase.co/auth/v1/token?grant_type=password";
    private static final String URL_CADASTRO = "https://projeto.supabase.co/auth/v1/signup";
    private static final UUID ID_CONTA = UUID.fromString("6f1c0a52-8a3e-4c39-9d6a-1b2f3c4d5e6f");

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

    @Test
    public void cadastroSemConfirmacaoDeEmailJaDevolveASessao()
    {
        supabase.expect(paraOCadastro())
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andExpect(pedido -> Assertions.assertEquals(
                        json.readTree("{\"email\": \"rita@email.com\", \"password\": \"segredo1\"}"),
                        json.readTree(((MockClientHttpRequest) pedido).getBodyAsString())))
                .andRespond(MockRestResponseCreators.withSuccess("""
                        {"access_token": "eyJ...", "token_type": "bearer", "expires_in": 3600, "refresh_token": "r1",
                         "user": {"id": "6f1c0a52-8a3e-4c39-9d6a-1b2f3c4d5e6f", "email": "rita@email.com",
                         "identities": [{"provider": "email"}]}}
                        """, MediaType.APPLICATION_JSON));

        Optional<ContaCriada> conta = auth.criar("rita@email.com", "segredo1");

        Assertions.assertEquals(Optional.of(new ContaCriada(ID_CONTA, new SessaoAutenticada(ID_CONTA, "eyJ...", 3600))), conta);
        supabase.verify();
    }

    @Test
    public void cadastroComConfirmacaoDeEmailDevolveSoAConta()
    {
        supabase.expect(paraOCadastro())
                .andRespond(MockRestResponseCreators.withSuccess("""
                        {"id": "6f1c0a52-8a3e-4c39-9d6a-1b2f3c4d5e6f", "email": "rita@email.com",
                         "confirmation_sent_at": "2026-10-30T13:00:00Z", "identities": [{"provider": "email"}]}
                        """, MediaType.APPLICATION_JSON));

        ContaCriada conta = auth.criar("rita@email.com", "segredo1").orElseThrow();

        Assertions.assertEquals(ID_CONTA, conta.authUserId());
        Assertions.assertTrue(conta.precisaConfirmarEmail());
    }

    @Test
    public void emailQueJaTemContaDevolveVazioNosDoisModos()
    {
        // Sem confirmação de e-mail o Supabase responde 422; com confirmação, um usuário sem identities.
        supabase.expect(paraOCadastro())
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": 422, \"error_code\": \"user_already_exists\", \"msg\": \"User already registered\"}"));
        supabase.expect(paraOCadastro())
                .andRespond(MockRestResponseCreators.withSuccess(
                        "{\"id\": \"6f1c0a52-8a3e-4c39-9d6a-1b2f3c4d5e6f\", \"identities\": []}", MediaType.APPLICATION_JSON));

        Assertions.assertEquals(Optional.empty(), auth.criar("rita@email.com", "segredo1"));
        Assertions.assertEquals(Optional.empty(), auth.criar("rita@email.com", "segredo1"));
    }

    @Test
    public void senhaFracaViraDadoInvalidoEOutroErroViraIndisponivel()
    {
        supabase.expect(paraOCadastro())
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": 422, \"error_code\": \"weak_password\", \"msg\": \"Password is too weak\"}"));
        supabase.expect(paraOCadastro())
                .andRespond(MockRestResponseCreators.withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": 400, \"error_code\": \"email_address_not_authorized\"}"));

        Assertions.assertThrows(DadosInvalidosException.class, () -> auth.criar("rita@email.com", "123456"));
        Assertions.assertThrows(AutenticacaoIndisponivelException.class, () -> auth.criar("rita@email.com", "segredo1"));
    }

    // O projeto não traz o Hamcrest, então a URL é conferida aqui em vez de MockRestRequestMatchers.requestTo.
    private static RequestMatcher paraOToken()
    {
        return pedido -> Assertions.assertEquals(URL_TOKEN, pedido.getURI().toString());
    }

    private static RequestMatcher paraOCadastro()
    {
        return pedido -> Assertions.assertEquals(URL_CADASTRO, pedido.getURI().toString());
    }
}
