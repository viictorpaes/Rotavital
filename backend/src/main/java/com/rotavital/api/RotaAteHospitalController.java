package com.rotavital.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.rota.OrigemRequest;
import com.rotavital.api.dto.rota.RotaAteHospitalDTO;
import com.rotavital.servico.RotaAteHospitalService;

/**
 * PI3-237 / PI3-262 — "Como chegar": rota do doador até o hospital do paciente da campanha.
 *
 * POST porque a posição do doador vai no corpo (fora da URL e dos logs) e porque cada chamada é
 * um cálculo novo no Google, não a leitura de um recurso guardado. Exige X-Usuario-Id de qualquer
 * perfil. Os erros (400, 401, 404) saem pelo TratadorDeErros.
 */
@RestController
@RequestMapping("/api/v1")
public class RotaAteHospitalController
{
    private final RotaAteHospitalService servico;

    public RotaAteHospitalController(RotaAteHospitalService servico)
    {
        this.servico = servico;
    }

    @PostMapping("/campanhas/{id}/rota")
    public ResponseEntity<RotaAteHospitalDTO> calcular(
            @RequestHeader(name = CampanhaController.HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("id") UUID campanhaId,
            @RequestBody(required = false) OrigemRequest origem)
    {
        Double latitude = origem == null ? null : origem.latitude();
        Double longitude = origem == null ? null : origem.longitude();
        return ResponseEntity.ok(RotaAteHospitalDTO.de(servico.calcular(usuarioId, campanhaId, latitude, longitude)));
    }
}
