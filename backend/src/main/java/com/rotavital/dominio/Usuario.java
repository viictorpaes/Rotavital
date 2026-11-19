package com.rotavital.dominio;

import java.time.LocalDate;
import java.time.Period;
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

    // O mesmo e-mail do login no Supabase Auth, em minúsculas (PI3-203).
    @Column(name = "email")
    private String email;

    // Perfil do doador (PI3-203): telefone só com dígitos e DDD; a data de nascimento confere a idade para doar.
    @Column(name = "telefone")
    private String telefone;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

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

    // Doador que se cadastrou pelo portal (PI3-203). A conta de login já foi criada no Supabase Auth.
    public static Usuario novoDoador(UUID authUserId, String nome, String email, TipoSanguineo tipoSanguineo,
            LocalDate dataNascimento, String telefone)
    {
        Usuario doador = new Usuario(UUID.randomUUID(), authUserId, nome, PapelUsuario.DOADOR, null, tipoSanguineo);
        doador.email = email;
        doador.dataNascimento = dataNascimento;
        doador.telefone = telefone;
        return doador;
    }

    // O doador corrige os próprios dados. O e-mail não muda aqui: é o login.
    public void atualizarPerfil(String nome, TipoSanguineo tipoSanguineo, LocalDate dataNascimento, String telefone)
    {
        this.nome = nome;
        this.tipoSanguineo = tipoSanguineo;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
    }

    // Idade completa na data informada; vazia para quem não informou a data de nascimento.
    public Integer idadeEm(LocalDate hoje)
    {
        return dataNascimento == null ? null : Period.between(dataNascimento, hoje).getYears();
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

    public String getEmail()
    {
        return email;
    }

    public String getTelefone()
    {
        return telefone;
    }

    public LocalDate getDataNascimento()
    {
        return dataNascimento;
    }
}
