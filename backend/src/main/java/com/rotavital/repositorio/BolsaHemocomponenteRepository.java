package com.rotavital.repositorio;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

// Consultas de bolsa na tabela bolsa_hemocomponente, pelo id (ex.: CH-1042).
public interface BolsaHemocomponenteRepository extends JpaRepository<BolsaHemocomponente, String>
{
    // Uma doação gera no máximo uma bolsa de cada componente (constraint uq_bolsa_lote_componente).
    boolean existsByLoteSinteticoAndTipoComponente(String loteSintetico, TipoComponente tipoComponente);

    // Na correção de uma bolsa, o lote só é repetido se outra bolsa (id diferente) já o usar.
    boolean existsByLoteSinteticoAndTipoComponenteAndIdNot(String loteSintetico, TipoComponente tipoComponente, String id);

    // Ids que começam com a sigla do componente (ex.: "CH" → CH-1042, CH-1043…), para gerar o próximo.
    @Query("select b.id from BolsaHemocomponente b where b.id like concat(:sigla, '-%')")
    List<String> buscarIdsComSigla(@Param("sigla") String sigla);

    // Listagem com filtros opcionais: filtro nulo não restringe nada. A página vem do Pageable
    // (limit/offset no banco de dados) e a ordem é FEFO, a mesma do estoque de um banco.
    @Query("""
            select b from BolsaHemocomponente b
            where (:bancoId is null or b.bancoOrigem.id = :bancoId)
              and (:tipoComponente is null or b.tipoComponente = :tipoComponente)
              and (:tipoSanguineo is null or b.tipoSanguineo = :tipoSanguineo)
              and (:status is null or b.status = :status)
            order by b.dataValidade asc, b.id asc
            """)
    List<BolsaHemocomponente> buscarComFiltros(@Param("bancoId") String bancoId,
            @Param("tipoComponente") TipoComponente tipoComponente,
            @Param("tipoSanguineo") TipoSanguineo tipoSanguineo,
            @Param("status") StatusBolsa status,
            Pageable pagina);

    // Mesma listagem, só com as bolsas ativas (PI3-243): sem as que já saíram do estoque de vez,
    // DESCARTADA e UTILIZADA.
    @Query("""
            select b from BolsaHemocomponente b
            where (:bancoId is null or b.bancoOrigem.id = :bancoId)
              and (:tipoComponente is null or b.tipoComponente = :tipoComponente)
              and (:tipoSanguineo is null or b.tipoSanguineo = :tipoSanguineo)
              and b.status not in (com.rotavital.dominio.StatusBolsa.DESCARTADA, com.rotavital.dominio.StatusBolsa.UTILIZADA)
            order by b.dataValidade asc, b.id asc
            """)
    List<BolsaHemocomponente> buscarAtivasComFiltros(@Param("bancoId") String bancoId,
            @Param("tipoComponente") TipoComponente tipoComponente,
            @Param("tipoSanguineo") TipoSanguineo tipoSanguineo,
            Pageable pagina);
}
