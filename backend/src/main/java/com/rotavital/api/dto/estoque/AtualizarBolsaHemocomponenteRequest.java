package com.rotavital.api.dto.estoque;

import java.time.LocalDate;

import com.rotavital.dominio.StatusBolsa;

/**
 * Corpo de PATCH /api/v1/hemocomponentes/{id} (PI3-190). Todos os campos são opcionais: o que não vier
 * fica como está. Por isso volumeMl e temperaturaCelsius são Double, e não double: com double, a falta
 * chegaria como 0. Localização em branco ("") apaga a localização. Componente, tipo sanguíneo, coleta,
 * lote e banco não mudam: se estiverem errados, a bolsa é descartada e cadastrada de novo.
 */
public record AtualizarBolsaHemocomponenteRequest(
        StatusBolsa status,
        LocalDate dataValidade,
        Double volumeMl,
        Double temperaturaCelsius,
        String localizacao)
{
    public boolean vazio()
    {
        return status == null && dataValidade == null && volumeMl == null && temperaturaCelsius == null
                && localizacao == null;
    }
}
