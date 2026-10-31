package com.rotavital.api;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.doador.AtualizarPerfilDoadorRequest;
import com.rotavital.api.dto.doador.CadastroDoadorDTO;
import com.rotavital.api.dto.doador.DoadorDTO;
import com.rotavital.api.dto.doador.NovoDoadorRequest;
import com.rotavital.dominio.Usuario;
import com.rotavital.servico.DoadorCadastrado;
import com.rotavital.servico.DoadorService;
import com.rotavital.servico.PerfilDoador;

/**
 * Cadastro e perfil do doador (PI3-203). Cadastrar é livre (é como o doador entra no sistema);
 * ver e alterar o perfil exigem o próprio doador no header X-Usuario-Id. Os erros saem pelo TratadorDeErros.
 */
@RestController
@RequestMapping("/api/v1")
public class DoadorController
{
    private static final String HEADER_USUARIO = CampanhaController.HEADER_USUARIO;

    private final DoadorService doadorService;
    private final Clock relogio;

    public DoadorController(DoadorService doadorService, Clock relogio)
    {
        this.doadorService = doadorService;
        this.relogio = relogio;
    }

    @PostMapping("/doadores")
    public ResponseEntity<CadastroDoadorDTO> cadastrar(@RequestBody(required = false) NovoDoadorRequest dados)
    {
        DoadorCadastrado cadastrado = doadorService.cadastrar(dados);
        Usuario doador = cadastrado.doador();

        DoadorDTO perfil = paraDTO(new PerfilDoador(doador, doador.idadeEm(LocalDate.now(relogio)), 0, null, null));
        CadastroDoadorDTO resposta = new CadastroDoadorDTO(perfil,
                cadastrado.precisaConfirmarEmail() ? null : AcessoController.paraDTO(doador, cadastrado.sessao()),
                cadastrado.precisaConfirmarEmail());

        return ResponseEntity.created(URI.create("/api/v1/doadores/eu")).body(resposta);
    }

    @GetMapping("/doadores/eu")
    public ResponseEntity<DoadorDTO> perfil(@RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId)
    {
        return ResponseEntity.ok(paraDTO(doadorService.perfil(usuarioId)));
    }

    @PutMapping("/doadores/eu")
    public ResponseEntity<DoadorDTO> atualizar(
            @RequestHeader(name = HEADER_USUARIO, required = false) UUID usuarioId,
            @RequestBody(required = false) AtualizarPerfilDoadorRequest dados)
    {
        return ResponseEntity.ok(paraDTO(doadorService.atualizar(usuarioId, dados)));
    }

    private static DoadorDTO paraDTO(PerfilDoador perfil)
    {
        Usuario doador = perfil.doador();
        return new DoadorDTO(
                doador.getId(),
                doador.getNome(),
                doador.getEmail(),
                doador.getDataNascimento(),
                perfil.idade(),
                doador.getTipoSanguineo(),
                doador.getTelefone(),
                perfil.doacoesRealizadas(),
                perfil.ultimaDoacao(),
                perfil.proximaDoacao()
        );
    }
}
