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
        Usuario usuario = autenticado.usuario();

        // Telas e menu por perfil: seções 3 e 4 de docs/PERFIS_DE_ACESSO.md.
        AcessoDTO acesso = switch (usuario.getPapel())
        {
            case MEDICO -> acesso(autenticado, TipoAcesso.MEDICO, "/painel", MENU_OPERACIONAL);
            case DOADOR -> acesso(autenticado, TipoAcesso.DOADOR, "/portal-doador", List.of());
            case ADMIN -> acesso(autenticado, TipoAcesso.ADMIN, "/admin", MENU_ADMIN);
        };

        return ResponseEntity.ok(acesso);
    }

    private static AcessoDTO acesso(UsuarioAutenticado autenticado, TipoAcesso tipo, String telaInicial, List<String> menu)
    {
        return new AcessoDTO(autenticado.usuario().getId(), autenticado.usuario().getNome(), tipo, telaInicial, menu,
                autenticado.sessao().accessToken(), autenticado.sessao().expiraEmSegundos());
    }
}
