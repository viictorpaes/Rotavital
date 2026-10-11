package com.rotavital.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rotavital.dominio.BolsaHemocomponente;

// Consultas de bolsa na tabela bolsa_hemocomponente, pelo id (ex.: CH-1042).
public interface BolsaHemocomponenteRepository extends JpaRepository<BolsaHemocomponente, String>
{
}
