package com.rotavital.api;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.UsuarioRepository;
import com.rotavital.servico.AutenticacaoIndisponivelException;
import com.rotavital.servico.AutenticacaoService;
import com.rotavital.servico.RepositorioFalso;
import com.rotavital.servico.SessaoAutenticada;
import com.rotavital.servico.VerificadorDeCredenciais;

// POST /api/v1/acessos do jeito que a tela recebe: perfil e tela inicial no 200, ErroDTO nos erros.
public class AcessoControllerTest
{
    private static final String SENHA = "senha-de-teste";

    private final ObjectMapper json = new ObjectMapper();

    // Um usuário de cada perfil; o e-mail "medico@", "doador@" ou "admin@" decide qual entra.
    private final VerificadorDeCredenciais supabaseFalso = (email, senha) ->
    {
        if (!SENHA.equals(senha))
        {
            return Optional.empty();
        }
        return switch (email)
        {
            case "medico@rotavital.dev" -> sessao(1);
            case "doador@rotavital.dev" -> sessao(2);
            case "admin@rotavital.dev" -> sessao(3);
            default -> Optional.empty();
        };
    };

    private final UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class,
            "findByAuthUserId", argumentos -> switch ((int) ((UUID) argumentos[0]).getLeastSignificantBits())
            {
                case 1 -> Optional.of(new Usuario(UUID.randomUUID(), authId(1), "Dra. Ana Lima", PapelUsuario.MEDICO, "HOSP-01", null));
                case 2 -> Optional.of(new Usuario(UUID.randomUUID(), authId(2), "Carlos Souza", PapelUsuario.DOADOR, null, TipoSanguineo.O_NEGATIVO));
                case 3 -> Optional.of(new Usuario(UUID.randomUUID(), authId(3), "Equipe Técnica", PapelUsuario.ADMIN, null, null));
                default -> Optional.empty();
            });

    private final MockMvc api = criarApi(supabaseFalso);

    @Test
    public void medicoVaiParaOPainel() throws Exception
    {
        JsonNode acesso = entrarComSucesso("medico@rotavital.dev");

        Assertions.assertEquals("Dra. Ana Lima", acesso.get("nome").asText());
        Assertions.assertEquals("MEDICO", acesso.get("tipoAcesso").asText());
        Assertions.assertEquals("/painel", acesso.get("telaInicial").asText());
        Assertions.assertEquals(6, acesso.get("menu").size());
        Assertions.assertEquals("token-1", acesso.get("accessToken").asText());
    }

    @Test
    public void doadorVaiParaOPortalSemMenu() throws Exception
    {
        JsonNode acesso = entrarComSucesso("doador@rotavital.dev");

        Assertions.assertEquals("DOADOR", acesso.get("tipoAcesso").asText());
        Assertions.assertEquals("/portal-doador", acesso.get("telaInicial").asText());
        Assertions.assertEquals(0, acesso.get("menu").size());
    }

    @Test
    public void adminVaiParaODiagnostico() throws Exception
    {
        JsonNode acesso = entrarComSucesso("admin@rotavital.dev");

        Assertions.assertEquals("ADMIN", acesso.get("tipoAcesso").asText());
        Assertions.assertEquals("/admin", acesso.get("telaInicial").asText());
        Assertions.assertEquals("Diagnóstico & Logs", acesso.get("menu").get(6).asText());
    }

    @Test
    public void senhaErradaEUsuarioInexistenteDevolvemO401Igual() throws Exception
    {
        ErroDTO esperado = new ErroDTO(null, "Não autorizado", 401, "E-mail ou senha incorretos.", "/api/v1/acessos");

        Assertions.assertEquals(esperado, erro(api, "{\"email\": \"medico@rotavital.dev\", \"senha\": \"errada\"}"));
        Assertions.assertEquals(esperado, erro(api, "{\"email\": \"ninguem@rotavital.dev\", \"senha\": \"" + SENHA + "\"}"));
    }

    @Test
    public void camposVaziosDevolvem400() throws Exception
    {
        Assertions.assertEquals(new ErroDTO(null, "Requisição inválida", 400,
                "O campo 'email' é obrigatório. O campo 'senha' é obrigatório.", "/api/v1/acessos"),
                erro(api, "{\"email\": \"\", \"senha\": \"\"}"));
    }

    @Test
    public void supabaseForaDoArDevolve503() throws Exception
    {
        MockMvc semSupabase = criarApi((email, senha) ->
        {
            throw new AutenticacaoIndisponivelException("Não foi possível conferir o login agora. Tente novamente em instantes.");
        });

        Assertions.assertEquals(new ErroDTO(null, "Serviço indisponível", 503,
                "Não foi possível conferir o login agora. Tente novamente em instantes.", "/api/v1/acessos"),
                erro(semSupabase, "{\"email\": \"medico@rotavital.dev\", \"senha\": \"" + SENHA + "\"}"));
    }

    private MockMvc criarApi(VerificadorDeCredenciais verificador)
    {
        return MockMvcBuilders.standaloneSetup(new AcessoController(new AutenticacaoService(verificador, usuarios)))
                .setControllerAdvice(new TratadorDeErros())
                .build();
    }

    private JsonNode entrarComSucesso(String email) throws Exception
    {
        MockHttpServletResponse resposta = post(api, "{\"email\": \"" + email + "\", \"senha\": \"" + SENHA + "\"}");

        Assertions.assertEquals(200, resposta.getStatus());
        return json.readTree(resposta.getContentAsString(StandardCharsets.UTF_8));
    }

    private ErroDTO erro(MockMvc alvo, String corpo) throws Exception
    {
        MockHttpServletResponse resposta = post(alvo, corpo);

        Assertions.assertEquals(MediaType.APPLICATION_PROBLEM_JSON_VALUE, resposta.getContentType());
        return json.readValue(resposta.getContentAsString(StandardCharsets.UTF_8), ErroDTO.class);
    }

    private static MockHttpServletResponse post(MockMvc alvo, String corpo) throws Exception
    {
        return alvo.perform(MockMvcRequestBuilders.post("/api/v1/acessos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andReturn().getResponse();
    }

    private static UUID authId(int n)
    {
        return new UUID(0, n);
    }

    private static Optional<SessaoAutenticada> sessao(int n)
    {
        return Optional.of(new SessaoAutenticada(authId(n), "token-" + n, 3600));
    }
}
