package com.rotavital.api.dto.estoque;

import java.time.LocalDate;

import com.rotavital.dominio.StatusBolsa;
import com.rotavital.dominio.TipoSanguineo;

/**
 * Corpo do PATCH /hemocomponentes/{id}. Serve para duas coisas, uma de cada vez:
 * mudar o status (status + motivoDescarte) ou corrigir dados do cadastro (os demais campos).
 * Na correção, campo ausente (null) mantém o valor atual; localizacao em branco apaga a localização.
 * volumeMl e temperaturaCelsius são Double para dar para saber quando não vieram.
 */
public record AtualizarBolsaRequest(
        StatusBolsa status,
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
    // Veio algum campo de correção? (status e motivoDescarte são da movimentação, não contam.)
    public boolean temCorrecao()
    {
        return tipoSanguineo != null || dataColeta != null || dataValidade != null || loteSintetico != null
                || volumeMl != null || temperaturaCelsius != null || localizacao != null;
    }
}
