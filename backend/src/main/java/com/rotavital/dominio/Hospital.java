package com.rotavital.dominio;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um hospital. Não herda de nada — tem um Endereco por
 * composição e implementa PontoDeRede para poder ser usado como nó no
 * grafo de rotas (AED).
 */
public class Hospital implements PontoDeRede {

    private String id;
    private String nome;
    private Endereco endereco;
    private List<RequisicaoHospitalar> requisicoes;

    public Hospital(String id, String nome, Endereco endereco) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.requisicoes = new ArrayList<>();
    }

    public List<RequisicaoHospitalar> getRequisicoes() { return requisicoes; }
    public Endereco getEndereco() { return endereco; }

    public RequisicaoHospitalar solicitar(TipoComponente tipoComponente,
                                           TipoSanguineo tipoSanguineo,
                                           int quantidade) {
        RequisicaoHospitalar requisicao = new RequisicaoHospitalar(
                this, tipoComponente, tipoSanguineo, quantidade);
        requisicoes.add(requisicao);
        return requisicao;
    }

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
        return "Hospital " + nome + " (" + endereco + ")";
    }
}
