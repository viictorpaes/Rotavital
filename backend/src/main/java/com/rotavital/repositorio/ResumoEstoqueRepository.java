package com.rotavital.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

// Totais do estoque lidos das views do painel (supabase/migrations/20260925120200_views_painel.sql).
// A contagem é feita no banco de dados: o backend recebe só os números, sem carregar as bolsas.
// Os apelidos entre aspas mantêm as maiúsculas, para baterem com os getters das projeções abaixo.
public interface ResumoEstoqueRepository extends Repository<BancoDeSangue, String>
{
    // Uma linha para cada um dos 8 tipos sanguíneos, inclusive os zerados, para todo banco de sangue
    // cadastrado. Para um id inexistente (ou de hospital) a lista vem vazia.
    @Query(value = """
            select tipo_sanguineo         as "tipoSanguineo",
                   unidades_disponiveis   as "unidades",
                   abaixo_nivel_seguranca as "abaixoNivelSeguranca"
            from vw_estoque_por_tipo
            where banco_id = :bancoId
            """, nativeQuery = true)
    List<TotalPorTipo> totaisPorTipo(@Param("bancoId") String bancoId);

    // Só as combinações de tipo e componente que têm bolsa. A view não filtra a validade,
    // por isso o filtro fica aqui: assim os totais batem com os de vw_estoque_por_tipo.
    @Query(value = """
            select tipo_sanguineo                as "tipoSanguineo",
                   tipo_componente               as "tipoComponente",
                   cast(sum(unidades) as integer) as "unidades"
            from vw_estoque_agrupado
            where banco_id = :bancoId
              and data_validade >= current_date
            group by tipo_sanguineo, tipo_componente
            """, nativeQuery = true)
    List<TotalPorTipoEComponente> totaisPorTipoEComponente(@Param("bancoId") String bancoId);

    interface TotalPorTipo
    {
        TipoSanguineo getTipoSanguineo();

        int getUnidades();

        boolean isAbaixoNivelSeguranca();
    }

    interface TotalPorTipoEComponente
    {
        TipoSanguineo getTipoSanguineo();

        TipoComponente getTipoComponente();

        int getUnidades();
    }
}
