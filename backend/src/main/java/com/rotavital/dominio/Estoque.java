package com.rotavital.dominio;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Estoque
{
    private final BancoDeSangue bancoDeSangue;

    public Estoque(BancoDeSangue bancoDeSangue)
    {
        this.bancoDeSangue = bancoDeSangue;
    }

    // As bolsas ficam no banco de sangue, que é a entidade gravada no banco de dados.
    // Assim, quando o banco vem do JPA, o estoque enxerga as bolsas carregadas.
    private List<BolsaHemocomponente> bolsas()
    {
        return bancoDeSangue.bolsas();
    }

    public void adicionarBolsa(BolsaHemocomponente bolsa)
    {
        bolsas().add(bolsa);
    }

    public List<BolsaHemocomponente> buscarDisponiveis(TipoComponente tipoComponente,
    TipoSanguineo tipoSanguineo)
    {
        return bolsas().stream()
        .filter(BolsaHemocomponente::estaDisponivel)
        .filter(b -> b.getTipoComponente() == tipoComponente)
        .filter(b -> b.getTipoSanguineo() == tipoSanguineo)
        .collect(Collectors.toList());
    }

    public List<BolsaHemocomponente> buscarPorTipoSanguineo(TipoSanguineo tipoSanguineo)
    {
        return bolsas().stream()
        .filter(b -> b.getTipoSanguineo() == tipoSanguineo)
        .collect(Collectors.toList());
    }

    /**
     * Lotes sugeridos para uma requisição (HU-04): só bolsas aptas e compatíveis com o paciente,
     * no máximo a quantidade pedida.
     */
    public List<BolsaHemocomponente> sugerirLotes(TipoComponente tipoComponente, TipoSanguineo tipoSanguineoPaciente,
    int quantidade, LocalDate hoje)
    {
        if (quantidade < 1)
        {
            throw new IllegalArgumentException("A quantidade pedida deve ser de pelo menos 1 bolsa.");
        }

        // Mesmo tipo do paciente primeiro: poupa o O-, que é o único que serve a todos.
        // Dentro de cada grupo, FEFO (vence primeiro, sai primeiro); o id desempata.
        Comparator<BolsaHemocomponente> ordem = Comparator
        .comparing((BolsaHemocomponente b) -> b.getTipoSanguineo() != tipoSanguineoPaciente)
        .thenComparing(BolsaHemocomponente::getDataValidade)
        .thenComparing(BolsaHemocomponente::getId);

        return bolsas().stream()
        .filter(b -> b.getTipoComponente() == tipoComponente)
        .filter(b -> b.estaAptaParaUso(hoje))
        .filter(b -> CompatibilidadeSanguinea.podeTransfundir(tipoComponente, b.getTipoSanguineo(), tipoSanguineoPaciente))
        .sorted(ordem)
        .limit(quantidade)
        .collect(Collectors.toList());
    }

    /**
     * Alertas de validade e temperatura das bolsas que ainda estão no estoque (HU-02).
     */
    public List<AlertaDeEstoque> listarAlertas(LocalDate hoje)
    {
        // TDD: só a assinatura, para o teste compilar e falhar. A regra entra no próximo commit.
        return List.of();
    }

    public List<BolsaHemocomponente> listarVencidas(LocalDate dataReferencia)
    {
        return bolsas().stream()
        .filter(b -> b.estaVencida(dataReferencia))
        .collect(Collectors.toList());
    }

    public BancoDeSangue getBancoDeSangue()
    {
        return bancoDeSangue;
    }

    public List<BolsaHemocomponente> getBolsas()
    {
        return bolsas();
    }
}