package com.rotavital.api;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.estoque.AtualizarStatusBolsaRequest;
import com.rotavital.api.dto.estoque.BolsaHemocomponenteDTO;
import com.rotavital.servico.MovimentacaoDeBolsaService;

// HU-08 — baixa da bolsa ao concluir o procedimento (e as demais movimentações de status).
@RestController
@RequestMapping("/api/v1")
public class MovimentacaoDeBolsaController
{
    private final MovimentacaoDeBolsaService movimentacaoDeBolsaService;

    public MovimentacaoDeBolsaController(MovimentacaoDeBolsaService movimentacaoDeBolsaService)
    {
        this.movimentacaoDeBolsaService = movimentacaoDeBolsaService;
    }

    // Contrato: PATCH /hemocomponentes/{id} com {"status": "UTILIZADA"} (docs/openapi.yaml).
    @PatchMapping("/hemocomponentes/{id}")
    public BolsaHemocomponenteDTO alterarStatus(@PathVariable("id") String id,
    @RequestBody AtualizarStatusBolsaRequest dados)
    {
        return BolsaHemocomponenteDTO.de(movimentacaoDeBolsaService.alterarStatus(id, dados.status()));
    }
}
