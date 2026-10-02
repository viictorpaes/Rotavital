package com.rotavital.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rotavital.dominio.BancoDeSangue;

// Consultas de banco de sangue na tabela ponto_rede. Como BancoDeSangue é uma subclasse,
// o Hibernate já filtra por tipo = 'BANCO_DE_SANGUE': buscar o id de um hospital devolve vazio.
public interface BancoDeSangueRepository extends JpaRepository<BancoDeSangue, String>
{
}
