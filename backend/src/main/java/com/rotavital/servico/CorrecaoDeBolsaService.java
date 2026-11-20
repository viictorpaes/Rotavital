package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.estoque.AtualizarBolsaRequest;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.EdicaoNaoPermitidaException;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;

/**
 * Correção de dados digitados errado no cadastro de uma bolsa (HU-03, edição do estoque).
 * Só bolsa DISPONIVEL pode ser corrigida, e com as mesmas regras do cadastro. Médico ou admin corrige;
 * o tipo sanguíneo, o campo mais perigoso, só o admin. Componente e banco de origem não mudam.
 */
@Service
public class CorrecaoDeBolsaService
{
    private final BolsaHemocomponenteRepository bolsas;
    private final EstoqueService estoque;
    private final IdentificacaoService identificacao;
    private final Clock relogio;

    public CorrecaoDeBolsaService(BolsaHemocomponenteRepository bolsas, EstoqueService estoque,
            IdentificacaoService identificacao, Clock relogio)
    {
        this.bolsas = bolsas;
        this.estoque = estoque;
        this.identificacao = identificacao;
        this.relogio = relogio;
    }

    /**
     * Aplica os campos que vieram (null mantém o valor atual) e devolve a bolsa corrigida; a mudança é
     * gravada no fim da transação. Erros: 401/403 (quem pede), 404 (bolsa), 400 (campo mal preenchido)
     * e 422 (bolsa fora do estoque ou regra do cadastro quebrada), sem gravar nada.
     */
    @Transactional
    public BolsaHemocomponente corrigir(UUID usuarioId, String bolsaId, AtualizarBolsaRequest dados)
    {
        Usuario usuario = identificacao.exigir(usuarioId, "corrigir os dados de uma bolsa",
                PapelUsuario.MEDICO, PapelUsuario.ADMIN);

        BolsaHemocomponente bolsa = bolsas.findById(bolsaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhuma bolsa encontrada com id " + bolsaId + "."));

        try
        {
            bolsa.conferirSePodeSerCorrigida();
        }
        catch (EdicaoNaoPermitidaException e)
        {
            throw new RegraDeNegocioException(e.getMessage());
        }

        TipoSanguineo tipoSanguineo = dados.tipoSanguineo() == null ? bolsa.getTipoSanguineo() : dados.tipoSanguineo();

        if (tipoSanguineo != bolsa.getTipoSanguineo() && usuario.getPapel() != PapelUsuario.ADMIN)
        {
            throw new AcessoNegadoException("Só admin pode corrigir o tipo sanguíneo de uma bolsa.");
        }

        conferirCampos(dados);

        LocalDate dataColeta = dados.dataColeta() == null ? bolsa.getDataColeta() : dados.dataColeta();
        LocalDate dataValidade = dados.dataValidade() == null ? bolsa.getDataValidade() : dados.dataValidade();
        String lote = dados.loteSintetico() == null ? bolsa.getLoteSintetico() : dados.loteSintetico().strip();
        double volumeMl = dados.volumeMl() == null ? bolsa.getVolumeMl() : dados.volumeMl();
        double temperatura = dados.temperaturaCelsius() == null ? bolsa.getTemperaturaCelsius() : dados.temperaturaCelsius();
        String localizacao = localizacaoCorrigida(bolsa, dados.localizacao());

        estoque.validarCorrecao(bolsa, dataColeta, dataValidade, lote, volumeMl, temperatura, LocalDate.now(relogio));

        bolsa.corrigirCadastro(tipoSanguineo, dataColeta, dataValidade, lote, volumeMl, temperatura, localizacao);
        return bolsa;
    }

    // Campos que vieram, mas vazios ou impossíveis: 400, todos de uma vez, como no cadastro.
    private void conferirCampos(AtualizarBolsaRequest dados)
    {
        List<String> problemas = new ArrayList<>();

        if (dados.loteSintetico() != null && dados.loteSintetico().isBlank())
        {
            problemas.add("O lote não pode ficar em branco.");
        }
        if (dados.volumeMl() != null && dados.volumeMl() <= 0)
        {
            problemas.add("O volume deve ser maior que zero.");
        }

        if (!problemas.isEmpty())
        {
            throw new DadosInvalidosException(String.join(" ", problemas));
        }
    }

    // null mantém a localização atual; texto em branco apaga (a bolsa fica sem localização definida).
    private static String localizacaoCorrigida(BolsaHemocomponente bolsa, String localizacao)
    {
        if (localizacao == null)
        {
            return bolsa.getLocalizacao();
        }
        return localizacao.isBlank() ? null : localizacao.strip();
    }
}
