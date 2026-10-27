package com.rotavital.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.doacao.AgendamentoDTO;
import com.rotavital.api.dto.doacao.AtualizarStatusAgendamentoRequest;
import com.rotavital.api.dto.doacao.NovoAgendamentoRequest;
import com.rotavital.api.dto.doacao.RemarcarAgendamentoRequest;
import com.rotavital.dominio.AgendamentoDoacao;
import com.rotavital.servico.AgendamentoService;

/**
 * Agendamentos de doação de cada campanha (PI3-192). Todos os endpoints exigem o header
 * X-Usuario-Id: o doador agenda, remarca e cancela os seus; o médico acompanha os da campanha
 * e registra doação feita, falta ou cancelamento. Os erros saem pelo TratadorDeErros.
 */
@RestController
@RequestMapping("/api/v1")
public class AgendamentoController
{
    private static final String HEADER_USUARIO = CampanhaController.HEADER_USUARIO;

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService)
    {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/campanhas/{campanhaId}/agendamentos")
    public ResponseEntity<List<AgendamentoDTO>> listarDaCampanha(
            @RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("campanhaId") UUID campanhaId)
    {
        return ResponseEntity.ok(paraDTOs(agendamentoService.listarDaCampanha(usuarioId, campanhaId)));
    }

    @PostMapping("/campanhas/{campanhaId}/agendamentos")
    public ResponseEntity<AgendamentoDTO> agendar(
            @RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("campanhaId") UUID campanhaId,
            @RequestBody(required = false) NovoAgendamentoRequest dados)
    {
        AgendamentoDoacao agendamento = agendamentoService.agendar(usuarioId, campanhaId, dados);
        return ResponseEntity.created(URI.create("/api/v1/agendamentos/" + agendamento.getId()))
                .body(paraDTO(agendamento));
    }

    @GetMapping("/agendamentos/meus")
    public ResponseEntity<List<AgendamentoDTO>> listarMeus(
            @RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId)
    {
        return ResponseEntity.ok(paraDTOs(agendamentoService.listarDoDoador(usuarioId)));
    }

    @PatchMapping("/agendamentos/{id}")
    public ResponseEntity<AgendamentoDTO> remarcar(
            @RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("id") UUID id,
            @RequestBody(required = false) RemarcarAgendamentoRequest dados)
    {
        AgendamentoDoacao agendamento = agendamentoService.remarcar(usuarioId, id,
                dados == null ? null : dados.agendadoPara());
        return ResponseEntity.ok(paraDTO(agendamento));
    }

    @PatchMapping("/agendamentos/{id}/status")
    public ResponseEntity<AgendamentoDTO> mudarStatus(
            @RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId,
            @PathVariable("id") UUID id,
            @RequestBody(required = false) AtualizarStatusAgendamentoRequest dados)
    {
        AgendamentoDoacao agendamento = agendamentoService.mudarStatus(usuarioId, id,
                dados == null ? null : dados.status());
        return ResponseEntity.ok(paraDTO(agendamento));
    }

    private static List<AgendamentoDTO> paraDTOs(List<AgendamentoDoacao> agendamentos)
    {
        return agendamentos.stream().map(AgendamentoController::paraDTO).toList();
    }

    private static AgendamentoDTO paraDTO(AgendamentoDoacao agendamento)
    {
        return new AgendamentoDTO(
                agendamento.getId(),
                agendamento.getCampanha() == null ? null : agendamento.getCampanha().getId(),
                agendamento.getCampanha() == null ? null : agendamento.getCampanha().getPaciente().getNome(),
                agendamento.getDoador().getId(),
                agendamento.getDoador().getNome(),
                agendamento.getDoador().getTipoSanguineo(),
                agendamento.getBanco().getId(),
                agendamento.getBanco().getNome(),
                agendamento.getAgendadoPara(),
                agendamento.getStatus(),
                agendamento.getCriadoEm()
        );
    }
}
