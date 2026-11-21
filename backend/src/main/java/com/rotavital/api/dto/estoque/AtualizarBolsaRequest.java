package com.rotavital.api.dto.estoque;

import java.time.LocalDate;

import com.rotavital.dominio.CategoriaDescarte;
import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoSanguineo;

/**
 * Corpo do PATCH /hemocomponentes/{id}. Serve para duas coisas, uma de cada vez:
 * mudar o status ou corrigir dados do cadastro (os demais campos). No descarte (status DESCARTADA),
 * categoriaDescarte é o motivo padronizado e motivoDescarte, a observação em texto livre.
 * Na correção, campo ausente (null) mantém o valor atual; localizacao em branco apaga a localização.
 * volumeMl e temperaturaCelsius são Double para dar para saber quando não vieram.
 */
public record AtualizarBolsaRequest(
        StatusBolsa status,
        CategoriaDescarte categoriaDescarte,
        String motivoDescarte,
        TipoSanguineo tipoSanguineo,
        LocalDate dataColeta,
        LocalDate dataValidade,
        String loteSintetico,
        Double volumeMl,
        Double temperaturaCelsius,
        String localizacao
)
{
    // Veio algum campo de correção? (status e os campos do descarte são da movimentação, não contam.)
    public boolean temCorrecao()
    {
        return tipoSanguineo != null || dataColeta != null || dataValidade != null || loteSintetico != null
                || volumeMl != null || temperaturaCelsius != null || localizacao != null;
    }
}
