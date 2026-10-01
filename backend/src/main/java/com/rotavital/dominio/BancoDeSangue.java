package com.rotavital.dominio;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("BANCO_DE_SANGUE")
public class BancoDeSangue extends PontoDeRedeBase
{
    // Bolsas armazenadas neste banco (lado inverso de BolsaHemocomponente.bancoOrigem).
    @OneToMany(mappedBy = "bancoOrigem")
    private List<BolsaHemocomponente> bolsas = new ArrayList<>();

    @Transient
    private final Estoque estoque = new Estoque(this);

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected BancoDeSangue()
    {
    }

    public BancoDeSangue(String id, String nome, Endereco endereco)
    {
        super(id, nome, endereco);
    }

    public Estoque getEstoque()
    {
        return estoque;
    }

    // Usado só pelo Estoque: é sempre a lista atual, inclusive a que o JPA carrega do banco.
    List<BolsaHemocomponente> bolsas()
    {
        return bolsas;
    }

    @Override
    public String toString()
    {
        return "Banco de Sangue " + getNome() + " (" + getEndereco() + ")";
    }
}
