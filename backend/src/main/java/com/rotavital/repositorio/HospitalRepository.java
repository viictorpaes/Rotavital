package com.rotavital.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rotavital.dominio.Hospital;

// Consultas de hospital na tabela ponto_rede. Como Hospital é uma subclasse, o Hibernate já
// filtra por tipo = 'HOSPITAL': buscar o id de um banco de sangue devolve vazio.
public interface HospitalRepository extends JpaRepository<Hospital, String>
{
}
