package com.rotavital.aceitacao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

/**
 * Traduz o jeito que a equipe do hospital escreve nos cenários ("O-", "hemácias", "01/10/2026")
 * para os valores da API (O_NEGATIVO, HEMACIAS, 2026-10-01).
 */
final class Vocabulario
{
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Map<String, TipoSanguineo> TIPOS = Map.of(
            "A+", TipoSanguineo.A_POSITIVO, "A-", TipoSanguineo.A_NEGATIVO,
            "B+", TipoSanguineo.B_POSITIVO, "B-", TipoSanguineo.B_NEGATIVO,
            "AB+", TipoSanguineo.AB_POSITIVO, "AB-", TipoSanguineo.AB_NEGATIVO,
            "O+", TipoSanguineo.O_POSITIVO, "O-", TipoSanguineo.O_NEGATIVO);

    private static final Map<String, TipoComponente> COMPONENTES = Map.of(
            "hemácias", TipoComponente.HEMACIAS,
            "plaquetas", TipoComponente.PLAQUETAS,
            "plasma", TipoComponente.PLASMA,
            "crioprecipitado", TipoComponente.CRIOPRECIPITADO);

    private static final Map<String, StatusBolsa> STATUS = Map.of(
            "disponível", StatusBolsa.DISPONIVEL,
            "reservada", StatusBolsa.RESERVADA,
            "em trânsito", StatusBolsa.EM_TRANSITO,
            "entregue", StatusBolsa.ENTREGUE,
            "descartada", StatusBolsa.DESCARTADA,
            "utilizada", StatusBolsa.UTILIZADA);

    private Vocabulario()
    {
    }

    static TipoSanguineo tipoSanguineo(String texto)
    {
        return traduzir(TIPOS, texto, "tipo sanguíneo");
    }

    static TipoComponente componente(String texto)
    {
        return traduzir(COMPONENTES, texto, "componente");
    }

    static StatusBolsa status(String texto)
    {
        return traduzir(STATUS, texto, "status");
    }

    static LocalDate data(String texto)
    {
        return LocalDate.parse(texto.trim(), DATA);
    }

    private static <T> T traduzir(Map<String, T> valores, String texto, String nome)
    {
        T valor = valores.get(texto.trim());
        if (valor == null)
        {
            throw new IllegalArgumentException("O cenário usa um " + nome + " desconhecido: '" + texto
                    + "'. Valores aceitos: " + valores.keySet());
        }
        return valor;
    }
}
