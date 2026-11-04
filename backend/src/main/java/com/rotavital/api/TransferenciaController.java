package com.rotavital.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.rota.NovaTransferenciaRequest;
import com.rotavital.api.dto.rota.TransferenciaDTO;
import com.rotavital.dominio.StatusTransferencia;
import com.rotavital.dominio.TransferenciaDeBolsa;
import com.rotavital.servico.TransferenciaService;

// PI3-236 — Envio e recebimento de bolsas pela tela de Rede. Os erros saem pelo TratadorDeErros.
@RestController
@RequestMapping("/api/v1")
public class TransferenciaController
{
    private final TransferenciaService servico;

    public TransferenciaController(TransferenciaService servico)
    {
        this.servico = servico;
    }

    @GetMapping("/transferencias")
    public List<TransferenciaDTO> listar(@RequestParam(name = "status", required = false) StatusTransferencia status)
    {
        return servico.listar(status).stream().map(TransferenciaDTO::de).toList();
    }

    @PostMapping("/transferencias")
    public ResponseEntity<TransferenciaDTO> despachar(
            @RequestHeader(name = CampanhaController.HEADER_USUARIO, required = false) UUID usuarioId,
            @RequestBody NovaTransferenciaRequest dados)
    {
        TransferenciaDeBolsa transferencia = servico.despachar(usuarioId, dados.bolsaId(), dados.destinoId());

        return ResponseEntity.created(URI.create("/api/v1/transferencias/" + transferencia.getId()))
                .body(TransferenciaDTO.de(transferencia));
    }

    @PostMapping("/transferencias/{id}/recebimento")
    public TransferenciaDTO receber(
            @RequestHeader(name = CampanhaController.HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("id") UUID id)
    {
        return TransferenciaDTO.de(servico.receber(usuarioId, id));
    }
}
