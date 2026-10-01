package com.rotavital.dominio;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@DiscriminatorValue("HOSPITAL")
public class Hospital extends PontoDeRedeBase
{
    // Requisições ainda não são gravadas no banco (fora do escopo da PI3-122).
    @Transient
    private final List<RequisicaoHospitalar> requisicoes = new ArrayList<>();

    // Exigido pelo Hibernate para criar o objeto ao ler do banco; no código, use o construtor com parâmetros.
    protected Hospital()
    {
    }

    public Hospital(String id, String nome, Endereco endereco)
    {
        super(id, nome, endereco);
    }

    public List<RequisicaoHospitalar> getRequisicoes()
    {
        return requisicoes;
    }

    public RequisicaoHospitalar solicitar(TipoComponente tipoComponente,
    TipoSanguineo tipoSanguineo,int quantidade)
    {
        RequisicaoHospitalar requisicao = new RequisicaoHospitalar(
        this, tipoComponente, tipoSanguineo, quantidade);

        requisicoes.add(requisicao);
        return requisicao;
    }

    @Override
    public String toString()
    {
        return "Hospital " + getNome() + " (" + getEndereco() + ")";
    }
}
