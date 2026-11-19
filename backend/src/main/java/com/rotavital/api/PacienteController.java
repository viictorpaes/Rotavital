package com.rotavital.api;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.doacao.PacienteDTO;
import com.rotavital.api.dto.paciente.PacienteRequest;
import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.StatusPaciente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.servico.PacienteService;

/**
 * Pacientes (PI3-194). Consultar é livre; cadastrar, editar e remover exigem um médico no header
 * X-Usuario-Id, como nas campanhas. Os erros (400, 401, 403, 404, 422) saem pelo TratadorDeErros.
 */
@RestController
@RequestMapping("/api/v1")
public class PacienteController
{
    private final PacienteService pacienteService;
    private final Clock relogio;

    public PacienteController(PacienteService pacienteService, Clock relogio)
    {
        this.pacienteService = pacienteService;
        this.relogio = relogio;
    }

    // Filtros opcionais e combináveis; a lista vem na ordem da fila clínica (mais grave e mais antigo primeiro).
    @GetMapping("/pacientes")
    public ResponseEntity<List<PacienteDTO>> listar(
            @RequestParam(name = "status", required = false) StatusPaciente status,
            @RequestParam(name = "tipoSanguineo", required = false) TipoSanguineo tipoSanguineo,
            @RequestParam(name = "gravidade", required = false) Gravidade gravidade,
            @RequestParam(name = "hospitalId", required = false) String hospitalId)
    {
        LocalDate hoje = LocalDate.now(relogio);
        return ResponseEntity.ok(pacienteService.listar(status, tipoSanguineo, gravidade, hospitalId).stream()
                .map(paciente -> PacienteDTO.de(paciente, hoje))
                .toList());
    }

    @GetMapping("/pacientes/{id}")
    public ResponseEntity<PacienteDTO> consultar(@PathVariable("id") UUID id)
    {
        return ResponseEntity.ok(paraDTO(pacienteService.buscar(id)));
    }

    @PostMapping("/pacientes")
    public ResponseEntity<PacienteDTO> cadastrar(
            @RequestHeader(name = CampanhaController.HEADER_USUARIO, required = false) UUID usuarioId,
            @RequestBody(required = false) PacienteRequest dados)
    {
        Paciente paciente = pacienteService.cadastrar(usuarioId, dados);
        return ResponseEntity.created(URI.create("/api/v1/pacientes/" + paciente.getId())).body(paraDTO(paciente));
    }

    @PutMapping("/pacientes/{id}")
    public ResponseEntity<PacienteDTO> editar(
            @RequestHeader(name = CampanhaController.HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("id") UUID id,
            @RequestBody(required = false) PacienteRequest dados)
    {
        return ResponseEntity.ok(paraDTO(pacienteService.editar(usuarioId, id, dados)));
    }

    @DeleteMapping("/pacientes/{id}")
    public ResponseEntity<Void> remover(
            @RequestHeader(name = CampanhaController.HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("id") UUID id)
    {
        pacienteService.remover(usuarioId, id);
        return ResponseEntity.noContent().build();
    }

    private PacienteDTO paraDTO(Paciente paciente)
    {
        return PacienteDTO.de(paciente, LocalDate.now(relogio));
    }
}
