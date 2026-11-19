package com.rotavital.supabase;

import java.time.Duration;
import java.util.List;
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
import com.rotavital.servico.ContaCriada;
import com.rotavital.servico.CriadorDeContas;
import com.rotavital.servico.DadosInvalidosException;
import com.rotavital.servico.SessaoAutenticada;
import com.rotavital.servico.VerificadorDeCredenciais;

/**
 * Confere e-mail e senha no Supabase Auth pela API REST (POST /auth/v1/token?grant_type=password),
 * com a chave publicável do projeto. A senha nunca passa pelo nosso banco: quem guarda o hash é o
 * Supabase (auth.users).
 *
 * O Supabase responde 400 tanto para senha errada quanto para e-mail sem cadastro, então os dois
 * casos já chegam aqui iguais.
 *
 * Também cria a conta do doador que se cadastra pelo portal (POST /auth/v1/signup, PI3-203).
 */
@Component
public class SupabaseAuth implements VerificadorDeCredenciais, CriadorDeContas
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

    /**
     * Com "Confirm email" desligado no Supabase, o signup já devolve a sessão. Ligado, devolve só o
     * usuário, e a pessoa precisa confirmar o e-mail antes de entrar. Para um e-mail que já tem conta,
     * o Supabase responde 422 user_already_exists (sem confirmação) ou um usuário sem identities
     * (com confirmação, para não revelar quem tem conta): os dois viram vazio.
     */
    @Override
    public Optional<ContaCriada> criar(String email, String senha)
    {
        if (!chaveConfigurada)
        {
            throw new AutenticacaoIndisponivelException(
                    "Cadastro indisponível: a variável SUPABASE_KEY não está configurada no backend.");
        }

        try
        {
            RespostaCadastro resposta = cliente.post()
                    .uri("/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", email, "password", senha))
                    .retrieve()
                    .body(RespostaCadastro.class);

            if (resposta != null && resposta.accessToken() != null && resposta.user() != null && resposta.user().id() != null)
            {
                SessaoAutenticada sessao = new SessaoAutenticada(resposta.user().id(), resposta.accessToken(),
                        resposta.expiresIn());
                return Optional.of(new ContaCriada(resposta.user().id(), sessao));
            }
            if (resposta != null && resposta.id() != null)
            {
                if (resposta.identities() != null && resposta.identities().isEmpty())
                {
                    return Optional.empty();
                }
                return Optional.of(new ContaCriada(resposta.id(), null));
            }

            throw new AutenticacaoIndisponivelException("O Supabase Auth respondeu o cadastro sem o usuário criado.");
        }
        catch (HttpClientErrorException e)
        {
            String corpo = e.getResponseBodyAsString();
            if (corpo.contains("user_already_exists") || corpo.contains("email_exists"))
            {
                return Optional.empty();
            }
            if (corpo.contains("weak_password"))
            {
                throw new DadosInvalidosException("Senha fraca: escolha uma senha mais forte, com pelo menos 6 caracteres "
                        + "misturando letras e números.");
            }
            if (corpo.contains("email_address_invalid") || corpo.contains("validation_failed"))
            {
                throw new DadosInvalidosException("O e-mail " + email + " não foi aceito. Confira se está escrito certo.");
            }
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS)
            {
                LOG.warn("Supabase Auth limitou os cadastros: {}", corpo);
                throw new AutenticacaoIndisponivelException(
                        "Muitos cadastros em pouco tempo. Tente de novo em alguns minutos.", e);
            }
            // Ex.: email_address_not_authorized, quando o projeto exige confirmar o e-mail e o SMTP padrão
            // do Supabase só envia para a equipe. Quem resolve é a configuração do projeto, não o doador.
            LOG.error("Supabase Auth recusou o cadastro: {}", corpo);
            throw new AutenticacaoIndisponivelException(
                    "Não foi possível criar a conta agora. Tente novamente em instantes.", e);
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

    // O signup devolve a sessão (access_token + user) ou, quando o e-mail precisa ser confirmado, só o usuário.
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaCadastro(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") long expiresIn,
            UsuarioAuth user,
            UUID id,
            List<Object> identities
    )
    {
    }
}
