package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.dominio.AlertaDeEstoque;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.repositorio.BancoDeSangueRepository;

@Service
public class AlertasDoEstoqueService
{
    private final BancoDeSangueRepository bancos;
    private final Clock relogio;

    public AlertasDoEstoqueService(BancoDeSangueRepository bancos, Clock relogio)
    {
        this.bancos = bancos;
        this.relogio = relogio;
    }

    // Alertas de validade e temperatura do estoque de um banco (HU-02); a regra fica em Estoque.listarAlertas.
    @Transactional(readOnly = true)
    public List<AlertaDeEstoque> listar(String bancoId)
    {
        BancoDeSangue banco = bancos.findById(bancoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum banco de sangue encontrado com id " + bancoId + "."));

        return banco.getEstoque().listarAlertas(LocalDate.now(relogio));
    }
}
