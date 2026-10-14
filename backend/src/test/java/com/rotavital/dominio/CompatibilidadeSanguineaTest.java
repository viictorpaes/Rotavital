package com.rotavital.dominio;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// HU-04 · T4.1 — Compatibilidade ABO/Rh. Cada linha é um receptor e os doadores que ele aceita:
// a tabela inteira (8 × 8) está aqui, então qualquer combinação errada derruba o teste.
public class CompatibilidadeSanguineaTest
{
    // Hemácias levam os antígenos A/B e o Rh do doador: o receptor não pode ter anticorpo contra eles.
    @ParameterizedTest(name = "receptor {0} recebe hemácias de {1}")
    @CsvSource(delimiter = '|', textBlock = """
            O_NEGATIVO  | O_NEGATIVO
            O_POSITIVO  | O_NEGATIVO O_POSITIVO
            A_NEGATIVO  | O_NEGATIVO A_NEGATIVO
            A_POSITIVO  | O_NEGATIVO O_POSITIVO A_NEGATIVO A_POSITIVO
            B_NEGATIVO  | O_NEGATIVO B_NEGATIVO
            B_POSITIVO  | O_NEGATIVO O_POSITIVO B_NEGATIVO B_POSITIVO
            AB_NEGATIVO | O_NEGATIVO A_NEGATIVO B_NEGATIVO AB_NEGATIVO
            AB_POSITIVO | O_NEGATIVO O_POSITIVO A_NEGATIVO A_POSITIVO B_NEGATIVO B_POSITIVO AB_NEGATIVO AB_POSITIVO
            """)
    public void hemacias(TipoSanguineo receptor, String doadores)
    {
        Assertions.assertEquals(tipos(doadores), doadoresAceitos(TipoComponente.HEMACIAS, receptor));
    }

    @Test
    public void oNegativoDoaHemaciasParaQualquerReceptor()
    {
        for (TipoSanguineo receptor : TipoSanguineo.values())
        {
            Assertions.assertTrue(CompatibilidadeSanguinea.podeTransfundir(
                    TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO, receptor), "O- → " + receptor);
        }
    }

    @Test
    public void hemaciasRhPositivoNuncaVaoParaReceptorRhNegativo()
    {
        Set<TipoSanguineo> rhNegativos = tipos("O_NEGATIVO A_NEGATIVO B_NEGATIVO AB_NEGATIVO");
        Set<TipoSanguineo> rhPositivos = tipos("O_POSITIVO A_POSITIVO B_POSITIVO AB_POSITIVO");

        for (TipoSanguineo doador : rhPositivos)
        {
            for (TipoSanguineo receptor : rhNegativos)
            {
                Assertions.assertFalse(CompatibilidadeSanguinea.podeTransfundir(
                        TipoComponente.HEMACIAS, doador, receptor), doador + " → " + receptor);
            }
        }
    }

    static Set<TipoSanguineo> doadoresAceitos(TipoComponente componente, TipoSanguineo receptor)
    {
        Set<TipoSanguineo> aceitos = EnumSet.noneOf(TipoSanguineo.class);
        for (TipoSanguineo doador : TipoSanguineo.values())
        {
            if (CompatibilidadeSanguinea.podeTransfundir(componente, doador, receptor))
            {
                aceitos.add(doador);
            }
        }
        return aceitos;
    }

    static Set<TipoSanguineo> tipos(String nomes)
    {
        Set<TipoSanguineo> tipos = EnumSet.noneOf(TipoSanguineo.class);
        Arrays.stream(nomes.trim().split("\\s+")).map(TipoSanguineo::valueOf).forEach(tipos::add);
        return tipos;
    }
}
