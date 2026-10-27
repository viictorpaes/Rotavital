package com.rotavital.repositorio;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rotavital.dominio.Paciente;

// Consultas na tabela paciente. A campanha de doação cria e edita o paciente que divulga.
public interface PacienteRepository extends JpaRepository<Paciente, UUID>
{
}
