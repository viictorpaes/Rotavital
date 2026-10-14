package com.rotavital.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.estoque.AlertaDTO;
import com.rotavital.api.dto.estoque.AlertasDoEstoqueDTO;
import com.rotavital.dominio.AlertaDeEstoque;
import com.rotavital.servico.AlertasDoEstoqueService;

// HU-02 — caixa de avisos do Painel Operacional.
@RestController
@RequestMapping("/api/v1")
public class AlertasDoEstoqueController
{
    private final AlertasDoEstoqueService alertasDoEstoqueService;

    public AlertasDoEstoqueController(AlertasDoEstoqueService alertasDoEstoqueService)
    {
        this.alertasDoEstoqueService = alertasDoEstoqueService;
    }

    // Banco inexistente: 404 (TratadorDeErros).
    @GetMapping("/bancos/{bancoId}/alertas")
    public AlertasDoEstoqueDTO listar(@PathVariable("bancoId") String bancoId)
    {
        List<AlertaDTO> alertas = alertasDoEstoqueService.listar(bancoId).stream()
                .map(AlertaDTO::de)
                .toList();
        int criticos = (int) alertas.stream().filter(a -> a.nivel() == AlertaDeEstoque.Nivel.CRITICO).count();

        return new AlertasDoEstoqueDTO(bancoId, alertas.size(), criticos, alertas);
    }
}
