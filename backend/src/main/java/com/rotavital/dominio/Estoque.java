package com.rotavital.dominio;
import java.time.LocalDate;
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