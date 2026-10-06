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
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.servico.EstoqueService;

@RestController
@RequestMapping("/api/v1")
public class EstoqueController
{
    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService)
    {
        this.estoqueService = estoqueService;
    }

    @GetMapping("/bancos/{bancoId}/estoque")
    public ResponseEntity<?> consultarEstoque(@PathVariable("bancoId") String bancoId,
    @RequestParam(name = "tipoSanguineo", required = false) TipoSanguineo tipoSanguineo)
    {
        Optional<List<BolsaHemocomponente>> resultado = estoqueService.buscarBolsas(bancoId, tipoSanguineo);

        if (resultado.isEmpty())
        {
            ErroDTO erro = new ErroDTO(null, "Recurso não encontrado", 404,
                    "Nenhum banco de sangue encontrado com id " + bancoId, "/api/v1/bancos/" + bancoId + "/estoque");
            return ResponseEntity.status(404).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(erro);
        }

        List<BolsaHemocomponente> bolsas = resultado.get();

        List<BolsaHemocomponenteDTO> bolsasDTO = new ArrayList<>();

        for (BolsaHemocomponente bolsa : bolsas)
        {
            bolsasDTO.add(new BolsaHemocomponenteDTO(
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
            ));
        }

        EstoqueDTO estoque = new EstoqueDTO(bancoId, bolsasDTO.size(), bolsasDTO);
        return ResponseEntity.ok(estoque);
    }
}