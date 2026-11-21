package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.estoque.ResumoPerdasDTO;
import com.rotavital.dominio.ResumoDePerdas;
import com.rotavital.repositorio.BancoDeSangueRepository;

// Painel de perdas da tela de Estoque (PI3-243). O cálculo fica em ResumoDePerdas.
@Service
public class ResumoPerdasService
{
    static final int PERIODO_MAXIMO_DIAS = 3650;

    private final BancoDeSangueRepository bancos;
    private final Clock relogio;

    public ResumoPerdasService(BancoDeSangueRepository bancos, Clock relogio)
    {
        this.bancos = bancos;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public Optional<ResumoPerdasDTO> resumir(String bancoId)
    {
        return resumir(bancoId, null);
    }

    /**
     * Perdas dos últimos "dias" dias, contando hoje (7 = hoje e os 6 dias anteriores); sem
     * período, todo o histórico. Banco inexistente: vazio (404 no controller). Período fora de
     * 1 a 3650 dias: DadosInvalidosException (400).
     */
    @Transactional(readOnly = true)
    public Optional<ResumoPerdasDTO> resumir(String bancoId, Integer dias)
    {
        if (dias != null && (dias < 1 || dias > PERIODO_MAXIMO_DIAS))
        {
            throw new DadosInvalidosException("O parâmetro 'dias' deve estar entre 1 e " + PERIODO_MAXIMO_DIAS + ".");
        }

        LocalDate desde = dias == null ? null : LocalDate.now(relogio).minusDays(dias - 1L);

        return bancos.findById(bancoId)
                .map(banco -> ResumoPerdasDTO.de(ResumoDePerdas.calcular(banco.getEstoque().getBolsas(), desde), dias));
    }
}
