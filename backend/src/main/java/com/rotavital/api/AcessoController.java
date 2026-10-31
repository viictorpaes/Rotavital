package com.rotavital.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.acesso.AcessoDTO;
import com.rotavital.api.dto.acesso.NovoAcessoRequest;
import com.rotavital.api.dto.acesso.TipoAcesso;
import com.rotavital.dominio.Usuario;
import com.rotavital.servico.AutenticacaoService;
import com.rotavital.servico.SessaoAutenticada;
import com.rotavital.servico.UsuarioAutenticado;

@RestController
@RequestMapping("/api/v1")
public class AcessoController
{
    private static final List<String> MENU_OPERACIONAL =
            List.of("Início", "Estoque", "Requisição", "Rede", "Pacientes", "Doações");
    private static final List<String> MENU_ADMIN =
            List.of("Início", "Estoque", "Requisição", "Rede", "Pacientes", "Doações", "Diagnóstico & Logs");

    private final AutenticacaoService autenticacaoService;

    public AcessoController(AutenticacaoService autenticacaoService)
    {
        this.autenticacaoService = autenticacaoService;
    }

    // Login com e-mail e senha. Os erros (400, 401, 503) saem pelo TratadorDeErros.
    @PostMapping("/acessos")
    public ResponseEntity<AcessoDTO> entrar(@RequestBody NovoAcessoRequest request)
    {
        UsuarioAutenticado autenticado = autenticacaoService.entrar(request.email(), request.senha());
        return ResponseEntity.ok(paraDTO(autenticado.usuario(), autenticado.sessao()));
    }

    // Telas e menu por perfil: seções 3 e 4 de docs/PERFIS_DE_ACESSO.md. O cadastro do doador
    // (DoadorController) devolve o mesmo acesso, para o portal já entrar.
    static AcessoDTO paraDTO(Usuario usuario, SessaoAutenticada sessao)
    {
        return switch (usuario.getPapel())
        {
            case MEDICO -> acesso(usuario, sessao, TipoAcesso.MEDICO, "/painel", MENU_OPERACIONAL);
            case DOADOR -> acesso(usuario, sessao, TipoAcesso.DOADOR, "/portal-doador", List.of());
            case ADMIN -> acesso(usuario, sessao, TipoAcesso.ADMIN, "/admin", MENU_ADMIN);
        };
    }

    private static AcessoDTO acesso(Usuario usuario, SessaoAutenticada sessao, TipoAcesso tipo, String telaInicial,
            List<String> menu)
    {
        return new AcessoDTO(usuario.getId(), usuario.getNome(), tipo, telaInicial, menu,
                sessao.accessToken(), sessao.expiraEmSegundos());
    }
}
