package com.rotavital.repositorio;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.StatusPaciente;
import com.rotavital.dominio.TipoSanguineo;

// Consultas na tabela paciente. O cadastro (PI3-194) e a campanha de doação (PI3-191) gravam por aqui.
public interface PacienteRepository extends JpaRepository<Paciente, UUID>
{
    // Filtro nulo não restringe. O hospital vem junto (join fetch), porque o DTO mostra o nome dele.
    @Query("""
            select p from Paciente p join fetch p.hospital h
            where (:status is null or p.status = :status)
              and (:tipoSanguineo is null or p.tipoSanguineo = :tipoSanguineo)
              and (:gravidade is null or p.gravidade = :gravidade)
              and (:hospitalId is null or h.id = :hospitalId)
            """)
    List<Paciente> buscarComFiltros(@Param("status") StatusPaciente status,
            @Param("tipoSanguineo") TipoSanguineo tipoSanguineo,
            @Param("gravidade") Gravidade gravidade,
            @Param("hospitalId") String hospitalId);

    // Registros que apontam para o paciente e impedem removê-lo (PI3-194): campanha, procedimento e requisição.
    @Query(value = "select count(*) from campanha_doacao where paciente_id = :id", nativeQuery = true)
    long contarCampanhas(@Param("id") UUID id);

    @Query(value = "select count(*) from procedimento where paciente_id = :id", nativeQuery = true)
    long contarProcedimentos(@Param("id") UUID id);

    @Query(value = "select count(*) from requisicao_hospitalar where paciente_id = :id", nativeQuery = true)
    long contarRequisicoes(@Param("id") UUID id);
}
