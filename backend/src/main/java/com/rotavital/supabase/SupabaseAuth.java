package com.rotavital.supabase;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rotavital.servico.AutenticacaoIndisponivelException;
import com.rotavital.servico.SessaoAutenticada;
import com.rotavital.servico.VerificadorDeCredenciais;

/**
 * Confere e-mail e senha no Supabase Auth pela API REST (POST /auth/v1/token?grant_type=password),
 * com a chave publicável do projeto. A senha nunca passa pelo nosso banco: quem guarda o hash é o
 * Supabase (auth.users).
 *
 * O Supabase responde 400 tanto para senha errada quanto para e-mail sem cadastro, então os dois
 * casos já chegam aqui iguais.
 */
@Component
public class SupabaseAuth implements VerificadorDeCredenciais
{
    private static final Logger LOG = LoggerFactory.getLogger(SupabaseAuth.class);
    private static final Duration TEMPO_LIMITE = Duration.ofSeconds(5);

    private final RestClient cliente;
    private final boolean chaveConfigurada;

    @Autowired
    public SupabaseAuth(@Value("${supabase.url}") String url, @Value("${supabase.publishable-key:}") String chave,
            RestClient.Builder builder)
    {
        this(montarCliente(url, chave, builder.requestFactory(fabricaComTempoLimite())), estaPreenchida(chave));
    }

    private SupabaseAuth(RestClient cliente, boolean chaveConfigurada)
    {
        this.cliente = cliente;
        this.chaveConfigurada = chaveConfigurada;
    }

    // Usado nos testes: mantém o RequestFactory do builder, que o MockRestServiceServer já trocou pelo dele.
    static SupabaseAuth semTempoLimite(String url, String chave, RestClient.Builder builder)
    {
        return new SupabaseAuth(montarCliente(url, chave, builder), estaPreenchida(chave));
    }

    @Override
    public Optional<SessaoAutenticada> verificar(String email, String senha)
    {
        if (!chaveConfigurada)
        {
            throw new AutenticacaoIndisponivelException(
                    "Login indisponível: a variável SUPABASE_KEY não está configurada no backend.");
        }

        try
        {
            RespostaToken resposta = cliente.post()
                    .uri("/token?grant_type=password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", email, "password", senha))
                    .retrieve()
                    .body(RespostaToken.class);

            if (resposta == null || resposta.user() == null || resposta.user().id() == null)
            {
                throw new AutenticacaoIndisponivelException("O Supabase Auth respondeu sem o usuário da sessão.");
            }

            return Optional.of(new SessaoAutenticada(resposta.user().id(), resposta.accessToken(), resposta.expiresIn()));
        }
        catch (HttpClientErrorException e)
        {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST)
            {
                // invalid_credentials é o caso normal; outro código (ex.: email_not_confirmed) indica
                // cadastro incompleto no Supabase, e fica no log para quem cadastrou o usuário.
                if (!e.getResponseBodyAsString().contains("invalid_credentials"))
                {
                    LOG.warn("Supabase Auth recusou o login: {}", e.getResponseBodyAsString());
                }
                return Optional.empty();
            }

            throw indisponivel(e);
        }
        catch (RestClientException e)
        {
            throw indisponivel(e);
        }
    }

    private static AutenticacaoIndisponivelException indisponivel(RestClientException e)
    {
        LOG.error("Falha ao consultar o Supabase Auth: {}", e.getMessage());
        return new AutenticacaoIndisponivelException(
                "Não foi possível conferir o login agora. Tente novamente em instantes.", e);
    }

    private static RestClient montarCliente(String url, String chave, RestClient.Builder builder)
    {
        return builder
                .baseUrl(url.replaceAll("/+$", "") + "/auth/v1")
                .defaultHeader("apikey", estaPreenchida(chave) ? chave : "")
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaToken(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn,
            UsuarioAuth user
    )
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UsuarioAuth(UUID id)
    {
    }
}
