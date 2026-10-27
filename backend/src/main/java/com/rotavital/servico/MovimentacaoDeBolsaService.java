package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.MovimentacaoInvalidaException;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;

@Service
public class MovimentacaoDeBolsaService
{
    private final BolsaHemocomponenteRepository bolsas;
    private final Clock relogio;

    public MovimentacaoDeBolsaService(BolsaHemocomponenteRepository bolsas, Clock relogio)
    {
        this.bolsas = bolsas;
        this.relogio = relogio;
    }

    /**
     * Muda o status de uma bolsa (HU-08: a baixa no procedimento é UTILIZADA). A regra de quais
     * transições valem fica em BolsaHemocomponente.alterarStatus; a mudança é gravada no fim da
     * transação. Bolsa inexistente: 404; status ausente: 400; transição recusada: 422, sem gravar nada.
     */
    @Transactional
    public BolsaHemocomponente alterarStatus(String bolsaId, StatusBolsa novoStatus)
    {
        if (novoStatus == null)
        {
            throw new DadosInvalidosException("O campo 'status' é obrigatório.");
        }

        BolsaHemocomponente bolsa = bolsas.findById(bolsaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma bolsa encontrada com id " + bolsaId + "."));

        try
        {
            bolsa.alterarStatus(novoStatus, LocalDate.now(relogio));
        }
        catch (MovimentacaoInvalidaException e)
        {
            throw new RegraDeNegocioException(e.getMessage());
        }

        return bolsa;
    }
}
