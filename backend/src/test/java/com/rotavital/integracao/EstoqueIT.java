package com.rotavital.integracao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

// Integração com o banco: a API lendo do Postgres criado pelas migrations do Supabase.
// O que só o banco garante (ordenação do JPA, contagem feita pelas views SQL) não aparece
// nos testes unitários, que usam repositórios falsos.
@TesteDeIntegracao
public class EstoqueIT
{
    @Autowired
    private TestRestTemplate api;

    @Autowired
    private JdbcTemplate jdbc;

    private DadosDeTeste dados;

    @BeforeEach
    public void bancoBS01Vazio()
    {
        dados = new DadosDeTeste(jdbc);
        dados.limpar();
        dados.bancoDeSangue("BS-01");
    }

    @Test
    public void migrationsCriamAs14TabelasEAsEntidadesBatemComElas()
    {
        // O contexto só sobe se o Hibernate (ddl-auto=validate) achar cada @Entity no schema.
        Integer tabelas = jdbc.queryForObject("""
                select count(*) from information_schema.tables
                where table_schema = 'public' and table_type = 'BASE TABLE'
                """, Integer.class);

        Assertions.assertEquals(14, tabelas);
    }

    @Test
    public void estoqueVemDoBancoDaValidadeMaisProximaParaAMaisDistante()
    {
        LocalDate hoje = LocalDate.now();
        dados.bolsa("CH-3", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, hoje.plusDays(30), 4.0, "R1", StatusBolsa.DISPONIVEL);
        dados.bolsa("CH-1", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, hoje.plusDays(3), 4.0, "R1", StatusBolsa.DISPONIVEL);
        dados.bolsa("CH-2", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.A_POSITIVO, hoje.plusDays(10), 4.0, "R1", StatusBolsa.DISPONIVEL);

        ResponseEntity<JsonNode> resposta = api.getForEntity("/api/v1/bancos/BS-01/estoque", JsonNode.class);

        Assertions.assertEquals(200, resposta.getStatusCode().value());
        Assertions.assertEquals(List.of("CH-1", "CH-2", "CH-3"), ids(resposta.getBody().get("bolsas")));
    }

    @Test
    public void resumoContaSoBolsasDisponiveisEDentroDaValidade()
    {
        // A contagem é da view vw_estoque_por_tipo (SQL), não do Java.
        LocalDate hoje = LocalDate.now();
        dados.bolsa("CH-1", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, hoje.plusDays(10), 4.0, "R1", StatusBolsa.DISPONIVEL);
        dados.bolsa("CH-2", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, hoje.plusDays(20), 4.0, "R1", StatusBolsa.DISPONIVEL);
        dados.bolsa("CH-3", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, hoje.minusDays(10), 4.0, "R1", StatusBolsa.DISPONIVEL);
        dados.bolsa("CH-4", "BS-01", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, hoje.plusDays(10), 4.0, "R1", StatusBolsa.DESCARTADA);

        JsonNode resumo = api.getForObject("/api/v1/bancos/BS-01/estoque/resumo", JsonNode.class);

        int unidadesONegativo = 0;
        for (JsonNode tipo : resumo.get("tiposSanguineos"))
        {
            if ("O_NEGATIVO".equals(tipo.get("tipoSanguineo").asText()))
            {
                unidadesONegativo = tipo.get("unidades").asInt();
            }
        }
        Assertions.assertEquals(2, unidadesONegativo);
        Assertions.assertEquals(2, resumo.get("totalUnidades").asInt());
    }

    private static List<String> ids(JsonNode bolsas)
    {
        List<String> ids = new ArrayList<>();
        bolsas.forEach(bolsa -> ids.add(bolsa.get("id").asText()));
        return ids;
    }
}
