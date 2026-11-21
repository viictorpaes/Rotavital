package com.rotavital.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.estoque.EstatisticasDoEstoqueDTO;
import com.rotavital.servico.EstatisticaDescritivaService;

// Estatística descritiva do estoque: distribuições de frequência e medidas de posição e dispersão.
@RestController
@RequestMapping("/api/v1")
public class EstatisticasDoEstoqueController
{
    private final EstatisticaDescritivaService estatisticaDescritivaService;

    public EstatisticasDoEstoqueController(EstatisticaDescritivaService estatisticaDescritivaService)
    {
        this.estatisticaDescritivaService = estatisticaDescritivaService;
    }

    // Banco inexistente: 404 (TratadorDeErros).
    @GetMapping("/bancos/{bancoId}/estoque/estatisticas")
    public EstatisticasDoEstoqueDTO calcular(@PathVariable("bancoId") String bancoId)
    {
        return EstatisticasDoEstoqueDTO.de(bancoId, estatisticaDescritivaService.calcular(bancoId));
    }
}
