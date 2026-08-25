package com.rotavital.dominio;

public final class BancoDeSangue implements PontoDeRede
{
    private final String id;
    private final String nome;
    private final Endereco endereco;
    private final Estoque estoque;

    public BancoDeSangue(String id, String nome, Endereco endereco)
    {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.estoque = new Estoque(this);
    }

    public Estoque getEstoque()
    {
        return estoque;
    }

    public Endereco getEndereco()
    {
        return endereco;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getNome()
    {
        return nome;
    }

    @Override
    public double getLatitude()
    {
        return endereco.getLatitude();
    }

    @Override
    public double getLongitude()
    {
        return endereco.getLongitude();
    }

    @Override
    public String toString()
    {
        return "Banco de Sangue " + nome + " (" + endereco + ")";
    }
}