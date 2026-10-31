package com.rotavital.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.doador.HemocentroDTO;
import com.rotavital.api.dto.doador.HorarioDTO;
import com.rotavital.config.RelogioConfig;
import com.rotavital.dominio.AgendaDoHemocentro;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.servico.AgendamentoService;

/**
 * Onde e quando doar (PI3-204, PI3-205): os hemocentros, com endereço e expediente, e os horários
 * livres de um dia. Consultar é livre: o Portal do Doador usa no lugar dos dados fixos que tinha.
 */
@RestController
@RequestMapping("/api/v1")
public class HemocentroController
{
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final AgendamentoService agendamentoService;

    public HemocentroController(AgendamentoService agendamentoService)
    {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/hemocentros")
    public ResponseEntity<List<HemocentroDTO>> listar()
    {
        return ResponseEntity.ok(agendamentoService.listarHemocentros().stream().map(HemocentroController::paraDTO).toList());
    }

    @GetMapping("/hemocentros/{id}")
    public ResponseEntity<HemocentroDTO> consultar(@PathVariable("id") String id)
    {
        return ResponseEntity.ok(paraDTO(agendamentoService.buscarHemocentro(id)));
    }

    // Domingo devolve a lista vazia: o hemocentro não abre.
    @GetMapping("/hemocentros/{id}/horarios")
    public ResponseEntity<List<HorarioDTO>> horarios(
            @PathVariable("id") String id,
            @RequestParam(name = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data)
    {
        List<HorarioDTO> horarios = agendamentoService.horariosDoDia(id, data).stream()
                .map(horario -> new HorarioDTO(
                        HORA.format(horario.inicio().atZoneSameInstant(RelogioConfig.FUSO)),
                        horario.inicio(),
                        horario.vagasRestantes(),
                        horario.disponivel()))
                .toList();
        return ResponseEntity.ok(horarios);
    }

    private static HemocentroDTO paraDTO(BancoDeSangue banco)
    {
        return new HemocentroDTO(
                banco.getId(),
                banco.getNome(),
                banco.getEndereco().getLogradouro(),
                banco.getEndereco().getCidade(),
                banco.getEndereco().getUf(),
                banco.getLatitude(),
                banco.getLongitude(),
                AgendaDoHemocentro.DESCRICAO,
                AgendaDoHemocentro.VAGAS_POR_HORARIO
        );
    }
}
