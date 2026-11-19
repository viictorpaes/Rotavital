package com.rotavital.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

import jakarta.persistence.LockModeType;

// Consultas de bolsa na tabela bolsa_hemocomponente, pelo id (ex.: CH-1042).
public interface BolsaHemocomponenteRepository extends JpaRepository<BolsaHemocomponente, String>
{
    // Uma doação gera no máximo uma bolsa de cada componente (constraint uq_bolsa_lote_componente).
    boolean existsByLoteSinteticoAndTipoComponente(String loteSintetico, TipoComponente tipoComponente);

    // Ids que começam com a sigla do componente (ex.: "CH" → CH-1042, CH-1043…), para gerar o próximo.
    @Query("select b.id from BolsaHemocomponente b where b.id like concat(:sigla, '-%')")
    List<String> buscarIdsComSigla(@Param("sigla") String sigla);

    // Trava de linha (SELECT ... FOR UPDATE) para quem muda o status da bolsa: duas movimentações da mesma
    // bolsa ao mesmo tempo passam uma depois da outra, e a segunda já lê o status gravado pela primeira.
    // Só vale dentro de uma transação.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BolsaHemocomponente b where b.id = :id")
    Optional<BolsaHemocomponente> travarPorId(@Param("id") String id);

    // Trava as bolsas disponíveis de um componente no banco antes da requisição escolher (HU-04): duas
    // requisições ao mesmo tempo não reservam a mesma bolsa, e a segunda vê o estoque que sobrou.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select b from BolsaHemocomponente b
            where b.bancoOrigem.id = :bancoId
              and b.tipoComponente = :tipoComponente
              and b.status = com.rotavital.dominio.StatusBolsa.DISPONIVEL
            """)
    List<BolsaHemocomponente> travarDisponiveis(@Param("bancoId") String bancoId,
            @Param("tipoComponente") TipoComponente tipoComponente);

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

    @Query(value = """
            select tipo_sanguineo as "categoria",
                   count(*) as "bolsas",
                   coalesce(sum(volume_ml), 0) as "volumeMl"
            from public.bolsa_hemocomponente
            where banco_origem_id = :bancoId and status = 'DESCARTADA'
            group by tipo_sanguineo
            order by sum(volume_ml) desc, count(*) desc, tipo_sanguineo asc
            """, nativeQuery = true)
    List<TotalPerda> perdasPorTipoSanguineo(@Param("bancoId") String bancoId);

    @Query(value = """
            select tipo_componente as "categoria",
                   count(*) as "bolsas",
                   coalesce(sum(volume_ml), 0) as "volumeMl"
            from public.bolsa_hemocomponente
            where banco_origem_id = :bancoId and status = 'DESCARTADA'
            group by tipo_componente
            order by sum(volume_ml) desc, count(*) desc, tipo_componente asc
            """, nativeQuery = true)
    List<TotalPerda> perdasPorComponente(@Param("bancoId") String bancoId);

    interface TotalPerda
    {
        String getCategoria();

        Long getBolsas();

        Double getVolumeMl();
    }
}
