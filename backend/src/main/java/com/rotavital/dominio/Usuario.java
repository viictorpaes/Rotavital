package com.rotavital.dominio;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Quem entra no sistema: médico, doador ou admin (tabela usuario).
 * A senha não fica aqui: o login é conferido pelo Supabase Auth, e authUserId liga
 * esta linha ao usuário de lá (auth.users).
 */
@Entity
@Table(name = "usuario")
public class Usuario
{
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "auth_user_id")
    private UUID authUserId;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false)
    private PapelUsuario papel;

    // Instituição do médico (hospital ou hemocentro); vazio para doador e admin.
    @Column(name = "ponto_rede_id")
    private String pontoRedeId;

    // Preenchido para doador.
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_sanguineo")
    private TipoSanguineo tipoSanguineo;

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected Usuario()
    {
    }

    public Usuario(UUID id, UUID authUserId, String nome, PapelUsuario papel, String pontoRedeId,
            TipoSanguineo tipoSanguineo)
    {
        this.id = id;
        this.authUserId = authUserId;
        this.nome = nome;
        this.papel = papel;
        this.pontoRedeId = pontoRedeId;
        this.tipoSanguineo = tipoSanguineo;
    }

    public UUID getId()
    {
        return id;
    }

    public UUID getAuthUserId()
    {
        return authUserId;
    }

    public String getNome()
    {
        return nome;
    }

    public PapelUsuario getPapel()
    {
        return papel;
    }

    public String getPontoRedeId()
    {
        return pontoRedeId;
    }

    public TipoSanguineo getTipoSanguineo()
    {
        return tipoSanguineo;
    }
}
