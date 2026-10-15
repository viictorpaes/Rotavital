package com.rotavital.dominio;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

public class FilaRequisicoesHospitalares
{
    private final Queue<RequisicaoHospitalar> requisicoes;

    public FilaRequisicoesHospitalares()
    {
        this.requisicoes = new ArrayDeque<>();
    }

    public void adicionar(RequisicaoHospitalar requisicao)
    {
        requisicoes.add(Objects.requireNonNull(requisicao, "A requisição não pode ser nula."));
    }

    public Optional<RequisicaoHospitalar> consultarProxima()
    {
        return Optional.ofNullable(requisicoes.peek());
    }

    public Optional<RequisicaoHospitalar> retirarProxima()
    {
        return Optional.ofNullable(requisicoes.poll());
    }

    public List<RequisicaoHospitalar> listar()
    {
        return List.copyOf(requisicoes);
    }
}
