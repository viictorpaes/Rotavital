package com.rotavital.dominio;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PilhaOperacoes<T>
{
    private final Deque<T> operacoes;

    public PilhaOperacoes()
    {
        this.operacoes = new ArrayDeque<>();
    }

    public void registrar(T operacao)
    {
        operacoes.push(Objects.requireNonNull(operacao, "A operação não pode ser nula."));
    }

    public Optional<T> consultarMaisRecente()
    {
        return Optional.ofNullable(operacoes.peek());
    }

    public Optional<T> removerMaisRecente()
    {
        return Optional.ofNullable(operacoes.poll());
    }

    public List<T> listar()
    {
        return List.copyOf(operacoes);
    }
}
