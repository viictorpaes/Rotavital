package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;

@Service
public class SugestaoDeLoteService
{
    private final BancoDeSangueRepository bancos;
    private final Clock relogio;

    public SugestaoDeLoteService(BancoDeSangueRepository bancos, Clock relogio)
    {
        this.bancos = bancos;
        this.relogio = relogio;
    }

    /**
     * Lotes sugeridos para um paciente (HU-04): a regra fica em Estoque.sugerirLotes; aqui só se
     * carrega o estoque do banco (dentro da transação, por causa do open-in-view=false).
     */
    @Transactional(readOnly = true)
    public List<BolsaHemocomponente> sugerir(String bancoId, TipoComponente tipoComponente,
            TipoSanguineo tipoSanguineoPaciente, int quantidade)
    {
        if (quantidade < 1)
        {
            throw new DadosInvalidosException("O campo 'quantidade' deve ser de pelo menos 1 bolsa.");
        }

        BancoDeSangue banco = bancos.findById(bancoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum banco de sangue encontrado com id " + bancoId + "."));

        return banco.getEstoque().sugerirLotes(tipoComponente, tipoSanguineoPaciente, quantidade, LocalDate.now(relogio));
    }
}
