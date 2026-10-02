package com.rotavital.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rotavital.dominio.PontoDeRedeBase;

// Todos os pontos da tabela ponto_rede: o Hibernate devolve Hospital ou BancoDeSangue conforme a coluna tipo.
public interface PontoDeRedeRepository extends JpaRepository<PontoDeRedeBase, String>
{
}
