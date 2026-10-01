package com.rotavital.dominio;
import java.util.List;
import java.util.Optional;

public class Hospital implements PontoDeRede
{
    private final String id;
    private final String nome;
    private final Endereco endereco;
    private final FilaRequisicoesHospitalares requisicoes;

    public Hospital(String id, String nome, Endereco endereco)
    {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.requisicoes = new FilaRequisicoesHospitalares();
    }

    public List<RequisicaoHospitalar> getRequisicoes()
    {
        return requisicoes.listar();
    }

    public Optional<RequisicaoHospitalar> consultarProximaRequisicao()
    {
        return requisicoes.consultarProxima();
    }

    public Optional<RequisicaoHospitalar> retirarProximaRequisicao()
    {
        return requisicoes.retirarProxima();
    }

    public Endereco getEndereco()
    {
        return endereco;
    }

    public RequisicaoHospitalar solicitar(TipoComponente tipoComponente,
                                           TipoSanguineo tipoSanguineo,
                                           int quantidade)
    {
        RequisicaoHospitalar requisicao = new RequisicaoHospitalar(
                this, tipoComponente, tipoSanguineo, quantidade);
        requisicoes.adicionar(requisicao);
        return requisicao;
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
        return "Hospital " + nome + " (" + endereco + ")";
    }
}