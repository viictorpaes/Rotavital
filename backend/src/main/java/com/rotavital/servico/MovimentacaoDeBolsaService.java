package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.CategoriaDescarte;
import com.rotavital.dominio.MovimentacaoInvalidaException;
import com.rotavital.dominio.PapelUsuario;
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
    private final IdentificacaoService identificacao;
    private final Clock relogio;

    static final int TAMANHO_MAXIMO_OBSERVACAO = 500;

    public MovimentacaoDeBolsaService(BolsaHemocomponenteRepository bolsas, TransferenciaDeBolsaRepository transferencias,
            IdentificacaoService identificacao, Clock relogio)
    {
        this.bolsas = bolsas;
        this.transferencias = transferencias;
        this.identificacao = identificacao;
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
        if (novoStatus == StatusBolsa.DESCARTADA)
        {
            throw new DadosInvalidosException("Para descartar a bolsa, informe o motivo do descarte (categoriaDescarte).");
        }

        BolsaHemocomponente bolsa = buscar(bolsaId);

        if (transferencias.findByBolsaIdAndStatus(bolsaId, StatusTransferencia.EM_TRANSITO).isPresent())
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
        return bolsa;
    }

    /**
     * Descarta uma bolsa (PI3-243): só o admin confirma (401 sem usuário, 403 para outro papel).
     * O motivo padronizado é obrigatório e a observação também quando o motivo é OUTRO (400). A
     * bolsa fica no banco com status DESCARTADA, motivo e data; descarte que quebra regra é 422.
     * Bolsa em transferência pode ser descartada (cadeia fria rompida no caminho), o que encerra
     * a transferência.
     */
    @Transactional
    public BolsaHemocomponente descartar(UUID usuarioId, String bolsaId, CategoriaDescarte categoria, String observacao)
    {
        identificacao.exigir(usuarioId, "descartar uma bolsa", PapelUsuario.ADMIN);

        String texto = observacao == null || observacao.isBlank() ? null : observacao.strip();
        if (categoria == null)
        {
            throw new DadosInvalidosException("Escolha o motivo do descarte (categoriaDescarte): "
                    + "VALIDADE_EXPIRADA, TEMPERATURA_FORA_DO_PADRAO, BOLSA_DANIFICADA ou OUTRO.");
        }
        if (categoria == CategoriaDescarte.OUTRO && texto == null)
        {
            throw new DadosInvalidosException("Descreva o motivo do descarte quando ele for \"Outro\".");
        }
        if (texto != null && texto.length() > TAMANHO_MAXIMO_OBSERVACAO)
        {
            throw new DadosInvalidosException("A observação do descarte deve ter no máximo "
                    + TAMANHO_MAXIMO_OBSERVACAO + " caracteres.");
        }

        BolsaHemocomponente bolsa = buscar(bolsaId);
        Optional<TransferenciaDeBolsa> transferencia =
                transferencias.findByBolsaIdAndStatus(bolsaId, StatusTransferencia.EM_TRANSITO);

        try
        {
            bolsa.descartar(categoria, texto, OffsetDateTime.now(relogio));
        }
        catch (MovimentacaoInvalidaException e)
        {
            throw new RegraDeNegocioException(e.getMessage());
        }

        transferencia.ifPresent(TransferenciaDeBolsa::cancelar);
        return bolsa;
    }

    private BolsaHemocomponente buscar(String bolsaId)
    {
        return bolsas.findById(bolsaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma bolsa encontrada com id " + bolsaId + "."));
    }
}
