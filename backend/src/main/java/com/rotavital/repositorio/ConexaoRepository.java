package com.rotavital.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rotavital.dominio.Conexao;

public interface ConexaoRepository extends JpaRepository<Conexao, Long>
{
    // Ordem de gravação (o id é gerado pelo banco em sequência).
    List<Conexao> findAllByOrderByIdAsc();
}
