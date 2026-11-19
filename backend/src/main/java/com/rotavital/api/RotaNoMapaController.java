package com.rotavital.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.rota.RotaNoMapaDTO;
import com.rotavital.servico.RotaNoMapaService;

/**
 * PI3-259 — Rota de transporte para desenhar no mapa da tela de Rede.
 *
 * Fica fora do GET /rotas de propósito: aquele só lê o grafo e a lista de hospitais o chama para cada
 * um; este chama o Google, então é pedido só para o hospital selecionado e exige X-Usuario-Id.
 * Os erros (400, 401, 403, 404, 422) saem pelo TratadorDeErros.
 */
@RestController
@RequestMapping("/api/v1")
public class RotaNoMapaController
{
    private final RotaNoMapaService servico;

    public RotaNoMapaController(RotaNoMapaService servico)
    {
        this.servico = servico;
    }

    @GetMapping("/rotas/trajeto")
    public ResponseEntity<RotaNoMapaDTO> calcular(
            @RequestHeader(name = CampanhaController.HEADER_USUARIO, required = false) UUID usuarioId,
            @RequestParam(name = "origemId", required = false) String origemId,
            @RequestParam(name = "destinoId", required = false) String destinoId)
    {
        return ResponseEntity.ok(RotaNoMapaDTO.de(servico.consultar(usuarioId, origemId, destinoId)));
    }
}
