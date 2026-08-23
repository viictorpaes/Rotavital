package com.rotavital.api.dto.comum;

public record ErroDTO(
        String type,
        String title,
        int status,
        String detail,
        String instance
)

{
    public ErroDTO
    {
        if (type == null || type.isBlank())
        {
            type = "about:blank";
        }
    }
}