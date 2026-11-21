package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.EstatisticaDescritivaDoEstoque;
import com.rotavital.repositorio.BancoDeSangueRepository;

// Estatística descritiva do estoque de um banco (disciplina de Estatística e Probabilidade).
// O cálculo fica no domínio, em EstatisticaDescritivaDoEstoque.
@Service
public class EstatisticaDescritivaService
{
    private final BancoDeSangueRepository bancos;
    private final Clock relogio;

    public EstatisticaDescritivaService(BancoDeSangueRepository bancos, Clock relogio)
    {
        this.bancos = bancos;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public EstatisticaDescritivaDoEstoque calcular(String bancoId)
    {
        BancoDeSangue banco = bancos.findById(bancoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum banco de sangue encontrado com id " + bancoId + "."));

        return EstatisticaDescritivaDoEstoque.calcular(banco.getEstoque().getBolsas(), LocalDate.now(relogio));
    }
}
