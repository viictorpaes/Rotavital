package com.rotavital.servico;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.api.dto.estoque.ResumoEstoqueDTO;
import com.rotavital.api.dto.estoque.TotalPorComponenteDTO;
import com.rotavital.api.dto.estoque.TotalPorTipoSanguineoDTO;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.ResumoEstoqueRepository;
import com.rotavital.repositorio.ResumoEstoqueRepository.TotalPorTipo;
import com.rotavital.repositorio.ResumoEstoqueRepository.TotalPorTipoEComponente;

// Montagem do resumo do estoque a partir das linhas das views do painel (no app, vw_estoque_por_tipo
// e vw_estoque_agrupado). Aqui as linhas são escritas à mão, como a view as devolveria.
public class ResumoEstoqueServiceTest
{
    // As projeções do repositório são interfaces com getters. Nestes records, o nome de cada
    // componente já é o nome do getter, então o record implementa a interface sem código extra.
    private record LinhaPorTipo(TipoSanguineo getTipoSanguineo, int getUnidades, boolean isAbaixoNivelSeguranca)
            implements TotalPorTipo
    {
    }

    private record LinhaPorTipoEComponente(TipoSanguineo getTipoSanguineo, TipoComponente getTipoComponente,
            int getUnidades) implements TotalPorTipoEComponente
    {
    }

    private ResumoEstoqueService servico;

    @BeforeEach
    public void criarViewsDoBancoBS01()
    {
        // Os 8 tipos, fora da ordem do enum: a view não garante ordem nenhuma.
        List<TotalPorTipo> porTipo = List.of(
                new LinhaPorTipo(TipoSanguineo.O_NEGATIVO, 12, false),
                new LinhaPorTipo(TipoSanguineo.AB_NEGATIVO, 0, true),
                new LinhaPorTipo(TipoSanguineo.A_POSITIVO, 3, true),
                new LinhaPorTipo(TipoSanguineo.B_NEGATIVO, 0, true),
                new LinhaPorTipo(TipoSanguineo.O_POSITIVO, 0, true),
                new LinhaPorTipo(TipoSanguineo.A_NEGATIVO, 0, true),
                new LinhaPorTipo(TipoSanguineo.AB_POSITIVO, 0, true),
                new LinhaPorTipo(TipoSanguineo.B_POSITIVO, 0, true));

        // Só as combinações com bolsa, como na consulta à vw_estoque_agrupado.
        List<TotalPorTipoEComponente> porTipoEComponente = List.of(
                new LinhaPorTipoEComponente(TipoSanguineo.O_NEGATIVO, TipoComponente.HEMACIAS, 8),
                new LinhaPorTipoEComponente(TipoSanguineo.A_POSITIVO, TipoComponente.PLAQUETAS, 3),
                new LinhaPorTipoEComponente(TipoSanguineo.O_NEGATIVO, TipoComponente.PLASMA, 4));

        // Só o BS-01 existe; para outro id as views não devolvem linha nenhuma.
        // O Repository do Spring Data não tem métodos próprios, então basta implementar as duas consultas.
        ResumoEstoqueRepository viewsFalsas = new ResumoEstoqueRepository()
        {
            @Override
            public List<TotalPorTipo> totaisPorTipo(String bancoId)
            {
                return "BS-01".equals(bancoId) ? porTipo : List.of();
            }

            @Override
            public List<TotalPorTipoEComponente> totaisPorTipoEComponente(String bancoId)
            {
                return "BS-01".equals(bancoId) ? porTipoEComponente : List.of();
            }
        };

        servico = new ResumoEstoqueService(viewsFalsas);
    }

    @Test
    public void bancoInexistenteDevolveVazio()
    {
        Assertions.assertTrue(servico.resumir("BS-99").isEmpty());
    }

    @Test
    public void devolveOsOitoTiposNaOrdemDoEnum()
    {
        List<TipoSanguineo> tipos = resumoDoBS01().tiposSanguineos().stream()
                .map(TotalPorTipoSanguineoDTO::tipoSanguineo)
                .toList();

        Assertions.assertEquals(List.of(TipoSanguineo.values()), tipos);
    }

    @Test
    public void divideCadaTipoPorComponenteComZeroNosQueFaltam()
    {
        Assertions.assertEquals(List.of(
                new TotalPorComponenteDTO(TipoComponente.HEMACIAS, 8),
                new TotalPorComponenteDTO(TipoComponente.PLASMA, 4),
                new TotalPorComponenteDTO(TipoComponente.PLAQUETAS, 0),
                new TotalPorComponenteDTO(TipoComponente.CRIOPRECIPITADO, 0)),
                doTipo(TipoSanguineo.O_NEGATIVO).componentes());

        // Tipo sem nenhuma bolsa: os quatro componentes aparecem zerados.
        Assertions.assertTrue(doTipo(TipoSanguineo.B_POSITIVO).componentes().stream()
                .allMatch(componente -> componente.unidades() == 0));
    }

    @Test
    public void somaOTotalGeralEOTotalDeCadaComponente()
    {
        ResumoEstoqueDTO resumo = resumoDoBS01();

        Assertions.assertEquals(15, resumo.totalUnidades());
        Assertions.assertEquals(List.of(
                new TotalPorComponenteDTO(TipoComponente.HEMACIAS, 8),
                new TotalPorComponenteDTO(TipoComponente.PLASMA, 4),
                new TotalPorComponenteDTO(TipoComponente.PLAQUETAS, 3),
                new TotalPorComponenteDTO(TipoComponente.CRIOPRECIPITADO, 0)),
                resumo.componentes());
    }

    @Test
    public void mantemOAlertaDeNivelDeSegurancaDaView()
    {
        Assertions.assertFalse(doTipo(TipoSanguineo.O_NEGATIVO).abaixoNivelSeguranca());
        Assertions.assertTrue(doTipo(TipoSanguineo.A_POSITIVO).abaixoNivelSeguranca());
        Assertions.assertEquals(3, doTipo(TipoSanguineo.A_POSITIVO).unidades());
    }

    private ResumoEstoqueDTO resumoDoBS01()
    {
        return servico.resumir("BS-01").orElseThrow();
    }

    private TotalPorTipoSanguineoDTO doTipo(TipoSanguineo tipo)
    {
        return resumoDoBS01().tiposSanguineos().stream()
                .filter(total -> total.tipoSanguineo() == tipo)
                .findFirst()
                .orElseThrow();
    }
}
