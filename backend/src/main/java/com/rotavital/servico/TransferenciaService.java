package com.rotavital.servico;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.MovimentacaoInvalidaException;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusTransferencia;
import com.rotavital.dominio.TransferenciaDeBolsa;
import com.rotavital.dominio.Trajeto;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.repositorio.HospitalRepository;
import com.rotavital.repositorio.TransferenciaDeBolsaRepository;

/**
 * PI3-236 — Envio de bolsas pela tela de Rede: do hemocentro ao hospital, com a rota calculada
 * pela rede (PI3-259) e a confirmação de chegada.
 *
 * A transação fica só em volta da gravação, com TransactionTemplate: a chamada ao Google, que
 * pode levar alguns segundos, acontece antes, sem prender uma conexão do banco.
 */
@Service
public class TransferenciaService
{
    private final TransferenciaDeBolsaRepository transferencias;
    private final BolsaHemocomponenteRepository bolsas;
    private final HospitalRepository hospitais;
    private final RotaNoMapaService rotas;
    private final IdentificacaoService identificacao;
    private final TransactionTemplate transacao;
    private final Clock relogio;

    public TransferenciaService(TransferenciaDeBolsaRepository transferencias, BolsaHemocomponenteRepository bolsas,
            HospitalRepository hospitais, RotaNoMapaService rotas, IdentificacaoService identificacao,
            PlatformTransactionManager gerenciadorDeTransacoes, Clock relogio)
    {
        this.transferencias = transferencias;
        this.bolsas = bolsas;
        this.hospitais = hospitais;
        this.rotas = rotas;
        this.identificacao = identificacao;
        this.transacao = new TransactionTemplate(gerenciadorDeTransacoes);
        this.relogio = relogio;
    }

    // Sem status: todas, da mais recente para a mais antiga.
    public List<TransferenciaDeBolsa> listar(StatusTransferencia status)
    {
        return status == null
                ? transferencias.findAllByOrderByDespachadaEmDesc()
                : transferencias.findByStatusOrderByDespachadaEmAsc(status);
    }

    /**
     * A bolsa sai do estoque do hemocentro dela para o hospital: fica EM_TRANSITO, com a localização
     * "Em transporte refrigerado → hospital" e a chegada prevista pela rota. Só um médico despacha.
     * Campos ausentes: 400; bolsa ou hospital inexistente: 404; bolsa que não pode sair ou hospital
     * sem caminho na rede: 422; duas saídas ao mesmo tempo: 409 (índice uq_transferencia_bolsa_ativa).
     */
    public TransferenciaDeBolsa despachar(UUID usuarioId, String bolsaId, String destinoId)
    {
        Usuario responsavel = identificacao.exigir(usuarioId, "despachar bolsas", PapelUsuario.MEDICO);

        if (bolsaId == null || bolsaId.isBlank() || destinoId == null || destinoId.isBlank())
        {
            throw new DadosInvalidosException("Informe bolsaId e destinoId.");
        }

        Hospital destino = hospitais.findById(destinoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum hospital encontrado com id " + destinoId + "."));

        BolsaHemocomponente bolsa = buscarBolsa(bolsaId);
        OffsetDateTime agora = OffsetDateTime.now(relogio);

        try
        {
            // Antes do Google: uma bolsa que não pode sair não gasta a cota da Routes API.
            bolsa.conferirEnvio(agora.toLocalDate());
            Trajeto trajeto = rotas.calcular(bolsa.getBancoOrigem().getId(), destinoId).trajeto();

            return transacao.execute(status -> transferencias.save(
                    TransferenciaDeBolsa.despachar(buscarBolsa(bolsaId), destino, trajeto, responsavel, agora)));
        }
        catch (MovimentacaoInvalidaException e)
        {
            throw new RegraDeNegocioException(e.getMessage());
        }
    }

    /**
     * Confirma a chegada no hospital: a bolsa fica ENTREGUE e passa a estar nele (hospital_atual_id e
     * localização). Só um médico confirma. Transferência inexistente: 404; já encerrada ou bolsa
     * que venceu no caminho: 422.
     */
    public TransferenciaDeBolsa receber(UUID usuarioId, UUID transferenciaId)
    {
        Usuario responsavel = identificacao.exigir(usuarioId, "confirmar o recebimento de bolsas", PapelUsuario.MEDICO);

        try
        {
            return transacao.execute(status ->
            {
                TransferenciaDeBolsa transferencia = transferencias.findById(transferenciaId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                "Nenhuma transferência encontrada com id " + transferenciaId + "."));

                transferencia.receber(responsavel, OffsetDateTime.now(relogio));
                return transferencia;
            });
        }
        catch (MovimentacaoInvalidaException e)
        {
            throw new RegraDeNegocioException(e.getMessage());
        }
    }

    private BolsaHemocomponente buscarBolsa(String bolsaId)
    {
        return bolsas.findById(bolsaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma bolsa encontrada com id " + bolsaId + "."));
    }
}
