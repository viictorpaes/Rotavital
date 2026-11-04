package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.MovimentacaoInvalidaException;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.StatusTransferencia;
import com.rotavital.dominio.TransferenciaDeBolsa;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.repositorio.TransferenciaDeBolsaRepository;

@Service
public class MovimentacaoDeBolsaService
{
    private final BolsaHemocomponenteRepository bolsas;
    private final TransferenciaDeBolsaRepository transferencias;
    private final Clock relogio;

    public MovimentacaoDeBolsaService(BolsaHemocomponenteRepository bolsas, TransferenciaDeBolsaRepository transferencias,
            Clock relogio)
    {
        this.bolsas = bolsas;
        this.transferencias = transferencias;
        this.relogio = relogio;
    }

    /**
     * Muda o status de uma bolsa (HU-08: a baixa no procedimento é UTILIZADA). A regra de quais
     * transições valem fica em BolsaHemocomponente.alterarStatus; a mudança é gravada no fim da
     * transação. Bolsa inexistente: 404; status ausente: 400; transição recusada: 422, sem gravar nada.
     *
     * Bolsa em transferência (PI3-236): a chegada é confirmada na transferência, para o hospital ficar
     * registrado; aqui só vale descartar (cadeia fria rompida no caminho), o que encerra a transferência.
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

        Optional<TransferenciaDeBolsa> transferencia =
                transferencias.findByBolsaIdAndStatus(bolsaId, StatusTransferencia.EM_TRANSITO);

        if (transferencia.isPresent() && novoStatus != StatusBolsa.DESCARTADA)
        {
            throw new RegraDeNegocioException("A bolsa " + bolsaId
                    + " está em transferência: confirme o recebimento pela tela de Rede.");
        }

        try
        {
            bolsa.alterarStatus(novoStatus, LocalDate.now(relogio));
        }
        catch (MovimentacaoInvalidaException e)
        {
            throw new RegraDeNegocioException(e.getMessage());
        }

        transferencia.ifPresent(TransferenciaDeBolsa::cancelar);
        return bolsa;
    }
}
