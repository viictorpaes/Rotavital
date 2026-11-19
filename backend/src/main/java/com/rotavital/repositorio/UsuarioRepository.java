package com.rotavital.repositorio;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rotavital.dominio.Usuario;

// Consultas na tabela usuario. O login acha a pessoa pelo id que o Supabase Auth devolve.
public interface UsuarioRepository extends JpaRepository<Usuario, UUID>
{
    Optional<Usuario> findByAuthUserId(UUID authUserId);

    // O cadastro do doador recusa um e-mail que já tem conta (PI3-203).
    boolean existsByEmailIgnoreCase(String email);
}
