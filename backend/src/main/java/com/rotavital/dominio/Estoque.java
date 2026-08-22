package com.rotavital.dominio;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controla o conjunto de bolsas de hemocomponentes de um banco de sangue.
 * Relação de composição: o Estoque não existe sem o BancoDeSangue ao qual
 * pertence.
 */
public class Estoque {

    private BancoDeSangue bancoDeSangue;
    private List<BolsaHemocomponente> bolsas;

    public Estoque(BancoDeSangue bancoDeSangue) {
        this.bancoDeSangue = bancoDeSangue;
        this.bolsas = new ArrayList<>();
    }

    public void adicionarBolsa(BolsaHemocomponente bolsa) {
        bolsas.add(bolsa);
    }

    public List<BolsaHemocomponente> buscarDisponiveis(TipoComponente tipoComponente,
                                                         TipoSanguineo tipoSanguineo) {
        return bolsas.stream()
                .filter(BolsaHemocomponente::estaDisponivel)
                .filter(b -> b.getTipoComponente() == tipoComponente)
                .filter(b -> b.getTipoSanguineo() == tipoSanguineo)
                .collect(Collectors.toList());
    }

    public List<BolsaHemocomponente> listarVencidas(LocalDate dataReferencia) {
        return bolsas.stream()
                .filter(b -> b.estaVencida(dataReferencia))
                .collect(Collectors.toList());
    }

    public BancoDeSangue getBancoDeSangue() {
        return bancoDeSangue;
    }

    public List<BolsaHemocomponente> getBolsas() {
        return bolsas;
    }
}
