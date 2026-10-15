package com.rotavital.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.api.dto.estoque.BolsaHemocomponenteDTO;
import com.rotavital.api.dto.estoque.EstoqueDTO;
import com.rotavital.api.dto.estoque.ResumoEstoqueDTO;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.servico.EstoqueService;
import com.rotavital.servico.ResumoEstoqueService;

@RestController
@RequestMapping("/api/v1")
public class EstoqueController
{
    private final EstoqueService estoqueService;
    private final ResumoEstoqueService resumoEstoqueService;

    public EstoqueController(EstoqueService estoqueService, ResumoEstoqueService resumoEstoqueService)
    {
        this.estoqueService = estoqueService;
        this.resumoEstoqueService = resumoEstoqueService;
    }

    @GetMapping("/bancos/{bancoId}/estoque/resumo")
    public ResponseEntity<?> resumirEstoque(@PathVariable("bancoId") String bancoId)
    {
        Optional<ResumoEstoqueDTO> resumo = resumoEstoqueService.resumir(bancoId);

        if (resumo.isEmpty())
        {
            return bancoNaoEncontrado(bancoId, "/api/v1/bancos/" + bancoId + "/estoque/resumo");
        }

        return ResponseEntity.ok(resumo.get());
    }

    @GetMapping("/bancos/{bancoId}/estoque")
    public ResponseEntity<?> consultarEstoque(@PathVariable("bancoId") String bancoId,
    @RequestParam(name = "tipoSanguineo", required = false) TipoSanguineo tipoSanguineo)
    {
        Optional<List<BolsaHemocomponente>> resultado = estoqueService.buscarBolsas(bancoId, tipoSanguineo);

        if (resultado.isEmpty())
        {
            return bancoNaoEncontrado(bancoId, "/api/v1/bancos/" + bancoId + "/estoque");
        }

        List<BolsaHemocomponente> bolsas = resultado.get();

        List<BolsaHemocomponenteDTO> bolsasDTO = new ArrayList<>();

        for (BolsaHemocomponente bolsa : bolsas)
        {
            bolsasDTO.add(paraBolsaDTO(bolsa));
        }

        EstoqueDTO estoque = new EstoqueDTO(bancoId, bolsasDTO.size(), bolsasDTO);
        return ResponseEntity.ok(estoque);
    }

    // Bolsa inexistente: o EstoqueService lança RecursoNaoEncontradoException e o TratadorDeErros responde 404.
    @GetMapping("/hemocomponentes/{id}")
    public ResponseEntity<BolsaHemocomponenteDTO> consultarBolsa(@PathVariable("id") String id)
    {
        return ResponseEntity.ok(paraBolsaDTO(estoqueService.buscarBolsa(id)));
    }

    private BolsaHemocomponenteDTO paraBolsaDTO(BolsaHemocomponente bolsa)
    {
        return new BolsaHemocomponenteDTO(
                bolsa.getId(),
                bolsa.getTipoComponente(),
                bolsa.getTipoSanguineo(),
                bolsa.getDataColeta(),
                bolsa.getDataValidade(),
                bolsa.getLoteSintetico(),
                bolsa.getVolumeMl(),
                bolsa.getTemperaturaCelsius(),
                bolsa.getLocalizacao(),
                bolsa.estaForaDaFaixa(),
                bolsa.getStatus(),
                bolsa.getBancoOrigem().getId()
        );
    }

    private ResponseEntity<ErroDTO> bancoNaoEncontrado(String bancoId, String caminho)
    {
        ErroDTO erro = new ErroDTO(null, "Recurso não encontrado", 404,
                "Nenhum banco de sangue encontrado com id " + bancoId, caminho);
        return ResponseEntity.status(404).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(erro);
    }
}