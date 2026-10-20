package com.rotavital.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.estoque.BolsaHemocomponenteDTO;
import com.rotavital.api.dto.estoque.SugestaoDeLoteDTO;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.servico.SugestaoDeLoteService;

// HU-04 — painel "Lote Sugerido (FEFO)" da tela de Requisição.
@RestController
@RequestMapping("/api/v1")
public class SugestaoDeLoteController
{
    private final SugestaoDeLoteService sugestaoDeLoteService;

    public SugestaoDeLoteController(SugestaoDeLoteService sugestaoDeLoteService)
    {
        this.sugestaoDeLoteService = sugestaoDeLoteService;
    }

    // Banco inexistente: 404; quantidade < 1: 400 (TratadorDeErros).
    @GetMapping("/bancos/{bancoId}/sugestao-lote")
    public SugestaoDeLoteDTO sugerir(@PathVariable("bancoId") String bancoId,
    @RequestParam("tipoComponente") TipoComponente tipoComponente,
    @RequestParam("tipoSanguineo") TipoSanguineo tipoSanguineo,
    @RequestParam("quantidade") int quantidade)
    {
        List<BolsaHemocomponenteDTO> lotes = sugestaoDeLoteService
                .sugerir(bancoId, tipoComponente, tipoSanguineo, quantidade).stream()
                .map(BolsaHemocomponenteDTO::de)
                .toList();

        return new SugestaoDeLoteDTO(bancoId, tipoComponente, tipoSanguineo, quantidade, lotes.size(),
                quantidade - lotes.size(), lotes);
    }
}
