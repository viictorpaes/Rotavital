package com.rotavital.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.estoque.AtualizarBolsaRequest;
import com.rotavital.api.dto.estoque.BolsaHemocomponenteDTO;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.servico.CorrecaoDeBolsaService;
import com.rotavital.servico.DadosInvalidosException;
import com.rotavital.servico.MovimentacaoDeBolsaService;

// HU-08 — baixa da bolsa ao concluir o procedimento (e as demais movimentações de status).
// HU-03 — correção dos dados de uma bolsa disponível, no mesmo PATCH (uma coisa de cada vez).
@RestController
@RequestMapping("/api/v1")
public class MovimentacaoDeBolsaController
{
    private final MovimentacaoDeBolsaService movimentacaoDeBolsaService;
    private final CorrecaoDeBolsaService correcaoDeBolsaService;

    public MovimentacaoDeBolsaController(MovimentacaoDeBolsaService movimentacaoDeBolsaService,
            CorrecaoDeBolsaService correcaoDeBolsaService)
    {
        this.movimentacaoDeBolsaService = movimentacaoDeBolsaService;
        this.correcaoDeBolsaService = correcaoDeBolsaService;
    }

    // Contrato (docs/openapi.yaml): {"status": "UTILIZADA"} muda o status; {"volumeMl": 430, ...} corrige
    // os dados. A correção exige o header X-Usuario-Id (médico ou admin; tipo sanguíneo só admin), e o
    // descarte {"status": "DESCARTADA", "categoriaDescarte": "VALIDADE_EXPIRADA"} exige admin (PI3-243).
    @PatchMapping("/hemocomponentes/{id}")
    public BolsaHemocomponenteDTO atualizar(@PathVariable("id") String id,
            @RequestHeader(name = CampanhaController.HEADER_USUARIO, required = false) UUID usuarioId,
            @RequestBody AtualizarBolsaRequest dados)
    {
        if (dados.temCorrecao() && dados.status() != null)
        {
            throw new DadosInvalidosException("Mude o status ou corrija os dados da bolsa, uma coisa de cada vez.");
        }
        if (!dados.temCorrecao() && dados.status() == null)
        {
            throw new DadosInvalidosException("Envie o novo status ou ao menos um campo para corrigir.");
        }

        BolsaHemocomponente bolsa;
        if (dados.temCorrecao())
        {
            bolsa = correcaoDeBolsaService.corrigir(usuarioId, id, dados);
        }
        else if (dados.status() == StatusBolsa.DESCARTADA)
        {
            bolsa = movimentacaoDeBolsaService.descartar(usuarioId, id, dados.categoriaDescarte(), dados.motivoDescarte());
        }
        else
        {
            bolsa = movimentacaoDeBolsaService.alterarStatus(id, dados.status());
        }

        return BolsaHemocomponenteDTO.de(bolsa);
    }
}
