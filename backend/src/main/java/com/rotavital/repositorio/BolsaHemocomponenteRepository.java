package com.rotavital.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.TipoComponente;

// Consultas de bolsa na tabela bolsa_hemocomponente, pelo id (ex.: CH-1042).
public interface BolsaHemocomponenteRepository extends JpaRepository<BolsaHemocomponente, String>
{
    // Uma doação gera no máximo uma bolsa de cada componente (constraint uq_bolsa_lote_componente).
    boolean existsByLoteSinteticoAndTipoComponente(String loteSintetico, TipoComponente tipoComponente);

    // Ids que começam com a sigla do componente (ex.: "CH" → CH-1042, CH-1043…), para gerar o próximo.
    @Query("select b.id from BolsaHemocomponente b where b.id like concat(:sigla, '-%')")
    List<String> buscarIdsComSigla(@Param("sigla") String sigla);
}
