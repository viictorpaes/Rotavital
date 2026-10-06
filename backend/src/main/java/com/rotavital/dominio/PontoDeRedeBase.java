package com.rotavital.dominio;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Base comum de Hospital e BancoDeSangue. Os dois ficam na mesma tabela (ponto_rede)
 * e a coluna "tipo" diz qual classe o Hibernate deve criar ao ler cada linha.
 */
@Entity
@Table(name = "ponto_rede")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo")
public abstract class PontoDeRedeBase implements PontoDeRede
{
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Embedded
    private Endereco endereco;

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected PontoDeRedeBase()
    {
    }

    protected PontoDeRedeBase(String id, String nome, Endereco endereco)
    {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
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

    public Endereco getEndereco()
    {
        return endereco;
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

    // Dois objetos com o mesmo id são o mesmo ponto: com JPA, a mesma linha pode ser carregada
    // em objetos diferentes, e o Dijkstra usa os pontos como chave de HashMap.
    @Override
    public boolean equals(Object outro)
    {
        if (this == outro)
        {
            return true;
        }

        if (!(outro instanceof PontoDeRedeBase ponto))
        {
            return false;
        }

        return getId() != null && getId().equals(ponto.getId());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(getId());
    }
}
