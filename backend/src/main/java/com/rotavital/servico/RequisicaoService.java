package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.requisicao.NivelUrgencia;
import com.rotavital.api.dto.requisicao.NovaRequisicaoRequest;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.MovimentacaoInvalidaException;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.repositorio.HospitalRepository;

/**
 * HU-04 — Requisição de hemocomponente para uma ala do hospital.
 *
 * Antes de aceitar o pedido, confere se o estoque do hemocentro tem bolsas aptas e compatíveis
 * para a quantidade inteira (a mesma regra FEFO da sugestão de lote). Faltando bolsa, nada é
 * reservado e a API responde 422. Havendo, cada bolsa escolhida fica RESERVADA e a localização
 * dela passa a ser o destino do pedido ("Hospital X · UTI Adulto · Leito 04").
 */
@Service
public class RequisicaoService
{
    /** Mesmo limite da constraint ck_requisicao_quantidade. */
    static final int QUANTIDADE_MAXIMA = 100;
    private static final int TAMANHO_MAXIMO_LEITO = 20;

    private final BancoDeSangueRepository bancos;
    private final HospitalRepository hospitais;
    private final BolsaHemocomponenteRepository bolsas;
    private final IdentificacaoService identificacao;
    private final Clock relogio;

    public RequisicaoService(BancoDeSangueRepository bancos, HospitalRepository hospitais,
            BolsaHemocomponenteRepository bolsas, IdentificacaoService identificacao, Clock relogio)
    {
        this.bancos = bancos;
        this.hospitais = hospitais;
        this.bolsas = bolsas;
        this.identificacao = identificacao;
        this.relogio = relogio;
    }

    /**
     * Registra o pedido e reserva as bolsas. Só um médico pede (401/403). Campo ausente ou fora do
     * limite: 400; banco ou hospital inexistente: 404; estoque sem bolsas suficientes: 422.
     */
    @Transactional
    public RequisicaoRegistrada registrar(UUID usuarioId, NovaRequisicaoRequest dados)
    {
        Usuario solicitante = identificacao.exigir(usuarioId, "fazer requisições de hemocomponente", PapelUsuario.MEDICO);
        validar(dados);

        Hospital hospital = hospitais.findById(dados.hospitalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum hospital encontrado com id " + dados.hospitalId() + "."));
        BancoDeSangue banco = bancos.findById(dados.bancoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum banco de sangue encontrado com id " + dados.bancoId() + "."));

        OffsetDateTime agora = OffsetDateTime.now(relogio);
        LocalDate hoje = agora.toLocalDate();
        int quantidade = dados.quantidade();

        List<BolsaHemocomponente> escolhidas = banco.getEstoque()
                .sugerirLotes(dados.tipoComponente(), dados.tipoSanguineo(), quantidade, hoje);

        if (escolhidas.size() < quantidade)
        {
            throw new RegraDeNegocioException(mensagemDeEstoqueInsuficiente(banco, dados, escolhidas.size()));
        }

        String leito = dados.leito() == null || dados.leito().isBlank() ? null : dados.leito().strip();
        String destino = dados.ala().localizacaoNo(hospital, leito);
        List<RequisicaoRegistrada.BolsaAlocada> alocadas = new ArrayList<>();

        try
        {
            for (BolsaHemocomponente bolsa : escolhidas)
            {
                String retirarDe = bolsa.getLocalizacao();
                bolsa.alocarPara(destino, hoje);
                alocadas.add(new RequisicaoRegistrada.BolsaAlocada(bolsa, retirarDe));
            }
        }
        catch (MovimentacaoInvalidaException e)
        {
            // A sugestão já filtra bolsas aptas; chegar aqui é uma corrida com outra movimentação.
            throw new RegraDeNegocioException(e.getMessage());
        }

        bolsas.saveAll(escolhidas);

        return new RequisicaoRegistrada(
                protocolo(),
                banco,
                hospital,
                dados.ala(),
                leito,
                destino,
                dados.tipoComponente(),
                dados.tipoSanguineo(),
                quantidade,
                dados.urgencia() == null ? NivelUrgencia.MEDIA : dados.urgencia(),
                dados.paciente() == null || dados.paciente().isBlank() ? null : dados.paciente().strip(),
                solicitante.getNome(),
                agora,
                alocadas);
    }

    private static void validar(NovaRequisicaoRequest dados)
    {
        if (dados == null)
        {
            throw new DadosInvalidosException("O corpo da requisição está vazio.");
        }

        List<String> problemas = new ArrayList<>();

        if (dados.bancoId() == null || dados.bancoId().isBlank())
        {
            problemas.add("O campo 'bancoId' é obrigatório.");
        }
        if (dados.hospitalId() == null || dados.hospitalId().isBlank())
        {
            problemas.add("O campo 'hospitalId' é obrigatório.");
        }
        if (dados.ala() == null)
        {
            problemas.add("O campo 'ala' é obrigatório.");
        }
        if (dados.tipoComponente() == null)
        {
            problemas.add("O campo 'tipoComponente' é obrigatório.");
        }
        if (dados.tipoSanguineo() == null)
        {
            problemas.add("O campo 'tipoSanguineo' é obrigatório.");
        }
        if (dados.quantidade() == null || dados.quantidade() < 1 || dados.quantidade() > QUANTIDADE_MAXIMA)
        {
            problemas.add("O campo 'quantidade' deve estar entre 1 e " + QUANTIDADE_MAXIMA + ".");
        }
        if (dados.leito() != null && dados.leito().strip().length() > TAMANHO_MAXIMO_LEITO)
        {
            problemas.add("O campo 'leito' aceita no máximo " + TAMANHO_MAXIMO_LEITO + " caracteres.");
        }

        if (!problemas.isEmpty())
        {
            throw new DadosInvalidosException(String.join(" ", problemas));
        }
    }

    private static String mensagemDeEstoqueInsuficiente(BancoDeSangue banco, NovaRequisicaoRequest dados, int aptas)
    {
        String disponiveis = switch (aptas)
        {
            case 0 -> "não há nenhuma bolsa apta e compatível";
            case 1 -> "há só 1 bolsa apta e compatível";
            default -> "há só " + aptas + " bolsas aptas e compatíveis";
        };

        return "Estoque insuficiente em " + banco.getNome() + ": " + disponiveis + " de " + dados.tipoComponente()
                + " para " + dados.tipoSanguineo() + ", e foram pedidas " + dados.quantidade()
                + ". Abra uma campanha de doação ou peça uma remessa inter-hospitalar pela tela de Rede.";
    }

    private static String protocolo()
    {
        return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
