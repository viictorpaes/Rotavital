package com.rotavital.dominio;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Estoque
{
    // "Vencendo em 7 dias" do card do painel (Figma, HU-02).
    public static final int DIAS_PARA_ALERTA_DE_VENCIMENTO = 7;

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        List<AlertaDeEstoque> alertas = new ArrayList<>();

        for (BolsaHemocomponente bolsa : bolsas())
        {
            // Só o que ainda está no estoque: bolsa descartada, em trânsito ou usada não é problema do painel.
            if (bolsa.getStatus() != StatusBolsa.DISPONIVEL && bolsa.getStatus() != StatusBolsa.RESERVADA)
            {
                continue;
            }

            long diasParaVencer = ChronoUnit.DAYS.between(hoje, bolsa.getDataValidade());
            if (diasParaVencer < 0)
            {
                alertas.add(alerta(bolsa, AlertaDeEstoque.Tipo.VENCIDA, AlertaDeEstoque.Nivel.CRITICO,
                "Bolsa " + bolsa.getId() + " venceu em " + DATA.format(bolsa.getDataValidade())
                + ": retirar do estoque e descartar."));
            }
            else if (diasParaVencer <= DIAS_PARA_ALERTA_DE_VENCIMENTO)
            {
                alertas.add(alerta(bolsa, AlertaDeEstoque.Tipo.VENCE_EM_BREVE, AlertaDeEstoque.Nivel.ATENCAO,
                "Bolsa " + bolsa.getId() + (diasParaVencer == 0 ? " vence hoje."
                : " vence em " + diasParaVencer + (diasParaVencer == 1 ? " dia" : " dias")
                + " (" + DATA.format(bolsa.getDataValidade()) + ").")));
            }

            if (bolsa.estaForaDaFaixa())
            {
                TipoComponente componente = bolsa.getTipoComponente();
                alertas.add(alerta(bolsa, AlertaDeEstoque.Tipo.FORA_DA_FAIXA, AlertaDeEstoque.Nivel.CRITICO,
                "Bolsa " + bolsa.getId() + " está a " + bolsa.getTemperaturaCelsius() + " °C, fora da faixa de "
                + componente.getTemperaturaMinima() + " a " + componente.getTemperaturaMaxima() + " °C."));
            }
        }

        alertas.sort(Comparator.comparing(AlertaDeEstoque::nivel)
        .thenComparing(AlertaDeEstoque::dataValidade)
        .thenComparing(AlertaDeEstoque::bolsaId)
        .thenComparing(AlertaDeEstoque::tipo));
        return alertas;
    }

    private static AlertaDeEstoque alerta(BolsaHemocomponente bolsa, AlertaDeEstoque.Tipo tipo,
    AlertaDeEstoque.Nivel nivel, String mensagem)
    {
        return new AlertaDeEstoque(bolsa.getId(), bolsa.getTipoComponente(), bolsa.getTipoSanguineo(),
        bolsa.getDataValidade(), tipo, nivel, mensagem);
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