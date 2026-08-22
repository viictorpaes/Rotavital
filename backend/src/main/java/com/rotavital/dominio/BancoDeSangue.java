package com.rotavital.dominio;

/**
 * Representa um banco de sangue. Não herda de nada — tem um Endereco por
 * composição e implementa PontoDeRede para poder ser usado como nó no
 * grafo de rotas (AED).
 */
public class BancoDeSangue implements PontoDeRede {

    private String id;
    private String nome;
    private Endereco endereco;
    private Estoque estoque;

    public BancoDeSangue(String id, String nome, Endereco endereco) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.estoque = new Estoque(this);
    }

    public Estoque getEstoque() { return estoque; }
    public Endereco getEndereco() { return endereco; }

    @Override
    public String getId() { return id; }

    @Override
    public String getNome() { return nome; }

    @Override
    public double getLatitude() { return endereco.getLatitude(); }

    @Override
    public double getLongitude() { return endereco.getLongitude(); }

    @Override
    public String toString() {
        return "Banco de Sangue " + nome + " (" + endereco + ")";
    }
}
