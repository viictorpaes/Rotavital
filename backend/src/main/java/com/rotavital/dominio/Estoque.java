package com.rotavital.dominio;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Estoque
{
    private final List<BolsaHemocomponente> bolsas;

    public Estoque()
    {
        this.bolsas = new ArrayList<>();
    }

    public void adicionarBolsa(BolsaHemocomponente bolsa)
    {
        bolsas.add(bolsa);
    }

    public List<BolsaHemocomponente> buscarDisponiveis(TipoComponente tipoComponente,
                                                         TipoSanguineo tipoSanguineo)
    {
        return bolsas.stream()
                .filter(BolsaHemocomponente::estaDisponivel)
                .filter(b -> b.getTipoComponente() == tipoComponente)
                .filter(b -> b.getTipoSanguineo() == tipoSanguineo)
                .toList();
    }

    public List<BolsaHemocomponente> buscarPorTipoSanguineo(TipoSanguineo tipoSanguineo)
    {
        return bolsas.stream()
                .filter(b -> b.getTipoSanguineo() == tipoSanguineo)
                .toList();
    }

    public List<BolsaHemocomponente> listarVencidas(LocalDate dataReferencia)
    {
        return bolsas.stream()
                .filter(b -> b.estaVencida(dataReferencia))
                .toList();
    }

    public List<BolsaHemocomponente> getBolsas()
    {
        return List.copyOf(bolsas);
    }
}