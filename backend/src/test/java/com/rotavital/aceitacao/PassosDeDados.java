package com.rotavital.aceitacao;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.integracao.DadosDeTeste;
import com.rotavital.integracao.RelogioDeTeste.RelogioAjustavel;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.pt.Dado;

// Passos "Dado" usados por todas as histórias: o dia de hoje e o estoque de um banco de sangue.
public class PassosDeDados
{
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private RelogioAjustavel relogio;

    private DadosDeTeste dados;

    @Before
    public void comecarComOBancoVazio()
    {
        dados = new DadosDeTeste(jdbc);
        dados.limpar();
    }

    @ParameterType("\\d{2}/\\d{2}/\\d{4}")
    public LocalDate data(String texto)
    {
        return Vocabulario.data(texto);
    }

    @Dado("que hoje é {data}")
    public void queHojeE(LocalDate hoje)
    {
        relogio.ajustarPara(hoje);
    }

    // Colunas: id, componente, tipo, validade e, opcionais, temperatura, localizacao e status.
    @Dado("o banco de sangue {string} com as bolsas:")
    public void oBancoComAsBolsas(String bancoId, DataTable tabela)
    {
        dados.bancoDeSangue(bancoId);

        for (Map<String, String> linha : tabela.asMaps())
        {
            TipoComponente componente = Vocabulario.componente(linha.get("componente"));
            String temperatura = linha.get("temperatura");
            String localizacao = linha.get("localizacao");
            String status = linha.get("status");

            dados.bolsa(linha.get("id"), bancoId, componente, Vocabulario.tipoSanguineo(linha.get("tipo")),
                    Vocabulario.data(linha.get("validade")),
                    temperatura == null ? temperaturaIdeal(componente) : Double.parseDouble(temperatura),
                    localizacao == null ? "R1 · P1 · N1" : localizacao,
                    status == null ? StatusBolsa.DISPONIVEL : Vocabulario.status(status));
        }
    }

    private static double temperaturaIdeal(TipoComponente componente)
    {
        return (componente.getTemperaturaMinima() + componente.getTemperaturaMaxima()) / 2;
    }
}
