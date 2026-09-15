package com.rotavital.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.acesso.AcessoDTO;
import com.rotavital.api.dto.acesso.NovoAcessoRequest;
import com.rotavital.api.dto.acesso.TipoAcesso;
import com.rotavital.api.dto.comum.ErroDTO;

@RestController
public class AcessoController
{
    @PostMapping("/acesso")
    public ResponseEntity<?> entrar(@RequestBody NovoAcessoRequest request)
    {
        if (request.nome() == null || request.nome().isBlank() || request.tipoAcesso() == null)
        {
            ErroDTO erro = new ErroDTO(null, "Requisição inválida", 400,
                    "Informe o nome e o tipo de acesso para entrar", "/acesso");
            return ResponseEntity.status(400).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(erro);
        }

        AcessoDTO acesso;

        if (request.tipoAcesso() == TipoAcesso.MEDICO)
        {
            acesso = new AcessoDTO(request.nome(), TipoAcesso.MEDICO, "Painel Operacional",
                    List.of("Início", "Estoque", "Requisição", "Rede", "Pacientes", "Doações"));
        }
        else
        {
            acesso = new AcessoDTO(request.nome(), TipoAcesso.DOADOR, "Portal do Doador", List.of());
        }

        return ResponseEntity.ok(acesso);
    }
}
