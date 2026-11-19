package com.rotavital.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.requisicao.NovaRequisicaoRequest;
import com.rotavital.api.dto.requisicao.RequisicaoRegistradaDTO;
import com.rotavital.servico.RequisicaoService;

// HU-04 — Requisição de hemocomponente com validação de estoque. Os erros saem pelo TratadorDeErros.
@RestController
@RequestMapping("/api/v1")
public class RequisicaoController
{
    private final RequisicaoService servico;

    public RequisicaoController(RequisicaoService servico)
    {
        this.servico = servico;
    }

    // 201 com as bolsas reservadas; 400 campo inválido; 404 banco/hospital; 422 estoque insuficiente.
    @PostMapping("/requisicoes")
    public ResponseEntity<RequisicaoRegistradaDTO> registrar(
            @RequestHeader(name = CampanhaController.HEADER_USUARIO, required = false) UUID usuarioId,
            @RequestBody(required = false) NovaRequisicaoRequest dados)
    {
        RequisicaoRegistradaDTO requisicao = RequisicaoRegistradaDTO.de(servico.registrar(usuarioId, dados));

        // Sem Location: o pedido não é gravado (ver RequisicaoService), então não há GET /requisicoes/{id}.
        return ResponseEntity.status(HttpStatus.CREATED).body(requisicao);
    }
}
