package com.rotavital.api;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.doacao.CampanhaDTO;
import com.rotavital.api.dto.doacao.CampanhaRequest;
import com.rotavital.api.dto.doacao.PacienteDTO;
import com.rotavital.dominio.CampanhaDoacao;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.StatusCampanha;
import com.rotavital.servico.CampanhaService;

/**
 * Campanhas de doação (PI3-191). Consultar é livre; publicar, editar e encerrar exigem um médico
 * no header X-Usuario-Id. Os erros (400, 401, 403, 404, 422) saem pelo TratadorDeErros.
 */
@RestController
@RequestMapping("/api/v1")
public class CampanhaController
{
    public static final String HEADER_USUARIO = "X-Usuario-Id";

    private final CampanhaService campanhaService;
    private final Clock relogio;

    public CampanhaController(CampanhaService campanhaService, Clock relogio)
    {
        this.campanhaService = campanhaService;
        this.relogio = relogio;
    }

    @GetMapping("/campanhas")
    public ResponseEntity<List<CampanhaDTO>> listar(
            @RequestParam(name = "status", required = false) StatusCampanha status)
    {
        Map<UUID, Long> emAberto = campanhaService.contarAgendamentosEmAberto();
        List<CampanhaDTO> lista = campanhaService.listar(status).stream()
                .map(campanha -> paraDTO(campanha, emAberto.getOrDefault(campanha.getId(), 0L)))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/campanhas/{id}")
    public ResponseEntity<CampanhaDTO> consultar(@PathVariable("id") UUID id)
    {
        return ResponseEntity.ok(comContagem(campanhaService.buscar(id)));
    }

    @PostMapping("/campanhas")
    public ResponseEntity<CampanhaDTO> publicar(
            @RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId,
            @RequestBody(required = false) CampanhaRequest dados)
    {
        CampanhaDoacao campanha = campanhaService.publicar(usuarioId, dados);
        return ResponseEntity.created(URI.create("/api/v1/campanhas/" + campanha.getId()))
                .body(paraDTO(campanha, 0));
    }

    @PutMapping("/campanhas/{id}")
    public ResponseEntity<CampanhaDTO> editar(
            @RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("id") UUID id,
            @RequestBody(required = false) CampanhaRequest dados)
    {
        return ResponseEntity.ok(comContagem(campanhaService.editar(usuarioId, id, dados)));
    }

    // Encerrar também cancela os agendamentos em aberto da campanha.
    @PatchMapping("/campanhas/{id}/encerramento")
    public ResponseEntity<CampanhaDTO> encerrar(
            @RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("id") UUID id)
    {
        return ResponseEntity.ok(comContagem(campanhaService.encerrar(usuarioId, id)));
    }

    private CampanhaDTO comContagem(CampanhaDoacao campanha)
    {
        long emAberto = campanhaService.contarAgendamentosEmAberto().getOrDefault(campanha.getId(), 0L);
        return paraDTO(campanha, emAberto);
    }

    private CampanhaDTO paraDTO(CampanhaDoacao campanha, long agendamentosEmAberto)
    {
        Paciente paciente = campanha.getPaciente();
        PacienteDTO pacienteDTO = new PacienteDTO(
                paciente.getId(),
                paciente.getNome(),
                paciente.getSexo(),
                paciente.getDataNascimento(),
                paciente.idadeEm(LocalDate.now(relogio)),
                paciente.getTipoSanguineo(),
                paciente.getTipoComponente(),
                paciente.getUnidadesNecessarias(),
                paciente.getCausa(),
                paciente.getGravidade(),
                paciente.getStatus(),
                paciente.getHospital().getId(),
                paciente.getHospital().getNome()
        );

        return new CampanhaDTO(
                campanha.getId(),
                campanha.getStatus(),
                campanha.getPublicadaEm(),
                campanha.getEncerradaEm(),
                campanha.getPublicadaPor() == null ? null : campanha.getPublicadaPor().getNome(),
                agendamentosEmAberto,
                pacienteDTO
        );
    }
}
