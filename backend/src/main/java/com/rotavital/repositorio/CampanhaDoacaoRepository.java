package com.rotavital.repositorio;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rotavital.dominio.CampanhaDoacao;
import com.rotavital.dominio.StatusCampanha;

// Consultas na tabela campanha_doacao, das mais recentes para as mais antigas.
public interface CampanhaDoacaoRepository extends JpaRepository<CampanhaDoacao, UUID>
{
    List<CampanhaDoacao> findAllByOrderByPublicadaEmDesc();

    List<CampanhaDoacao> findByStatusOrderByPublicadaEmDesc(StatusCampanha status);
}
