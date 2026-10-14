package com.rotavital.integracao;

import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;

import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

/**
 * Grava os dados de cada teste direto nas tabelas, com SQL: assim as constraints e gatilhos
 * das migrations também valem para os dados de teste.
 */
public class DadosDeTeste
{
    private final JdbcTemplate jdbc;

    public DadosDeTeste(JdbcTemplate jdbc)
    {
        this.jdbc = jdbc;
    }

    // Cada teste começa do zero. O cascade limpa também as tabelas que apontam para estas.
    public void limpar()
    {
        jdbc.execute("truncate table public.bolsa_hemocomponente, public.ponto_rede cascade");
    }

    public void bancoDeSangue(String id)
    {
        jdbc.update("""
                insert into public.ponto_rede (id, tipo, nome, logradouro, cidade, uf, latitude, longitude)
                values (?, 'BANCO_DE_SANGUE', ?, 'Av. Central, 100', 'Recife', 'PE', -8.0578, -34.8829)
                """, id, "Hemocentro " + id);
    }

    public void bolsa(String id, String bancoId, TipoComponente componente, TipoSanguineo tipo,
            LocalDate validade, double temperatura, String localizacao, StatusBolsa status)
    {
        jdbc.update("""
                insert into public.bolsa_hemocomponente
                    (id, banco_origem_id, tipo_componente, tipo_sanguineo, data_coleta, data_validade,
                     lote_sintetico, volume_ml, temperatura_celsius, localizacao, status)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id, bancoId, componente.name(), tipo.name(), validade.minusDays(diasDaColetaAteAValidade(componente)),
                validade, "LOTE-" + id, volume(componente), temperatura, localizacao, status.name());
    }

    public String statusDaBolsa(String id)
    {
        return jdbc.queryForObject("select status from public.bolsa_hemocomponente where id = ?", String.class, id);
    }

    // Dentro do prazo máximo de cada componente (ck_bolsa_prazo_maximo_por_componente).
    private static long diasDaColetaAteAValidade(TipoComponente componente)
    {
        return switch (componente)
        {
            case HEMACIAS -> 35;
            case PLAQUETAS -> 5;
            case PLASMA -> 365;
            case CRIOPRECIPITADO -> 300;
        };
    }

    private static double volume(TipoComponente componente)
    {
        return switch (componente)
        {
            case HEMACIAS -> 450.0;
            case PLAQUETAS -> 300.0;
            case PLASMA -> 200.0;
            case CRIOPRECIPITADO -> 30.0;
        };
    }
}
