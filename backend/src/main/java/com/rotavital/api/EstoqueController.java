package com.rotavital.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.api.dto.estoque.BolsaHemocomponenteDTO;
import com.rotavital.api.dto.estoque.EstoqueDTO;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.TipoSanguineo;

@RestController
public class EstoqueController
{
    private final BancosEmMemoria bancos;

    public EstoqueController(BancosEmMemoria bancos)
    {
        this.bancos = bancos;
    }

    @GetMapping("/estoque/{bancoId}")
    public ResponseEntity<?> consultarEstoque(@PathVariable String bancoId,
    @RequestParam(required = false) TipoSanguineo tipoSanguineo)
    {
        BancoDeSangue banco = bancos.buscarPorId(bancoId);

        if (banco == null)
        {
            ErroDTO erro = new ErroDTO(null, "Recurso não encontrado", 404,
                    "Nenhum banco de sangue encontrado com id " + bancoId, "/estoque/" + bancoId);
            return ResponseEntity.status(404).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(erro);
        }

        List<BolsaHemocomponente> bolsas;

        if (tipoSanguineo == null)
        {
            bolsas = banco.getEstoque().getBolsas();
        }

        else
        {
            bolsas = banco.getEstoque().buscarPorTipoSanguineo(tipoSanguineo);
        }

        List<BolsaHemocomponenteDTO> bolsasDTO = new ArrayList<>();

        for (BolsaHemocomponente bolsa : bolsas)
        {
            bolsasDTO.add(new BolsaHemocomponenteDTO(
                    bolsa.getId(),
                    bolsa.getTipoComponente(),
                    bolsa.getTipoSanguineo(),
                    bolsa.getDataColeta(),
                    bolsa.getDataValidade(),
                    null,
                    bolsa.getVolumeMl(),
                    bolsa.getTemperaturaCelsius(),
                    bolsa.getLocalizacao(),
                    bolsa.estaForaDaFaixa(),
                    bolsa.getStatus(),
                    bolsa.getBancoOrigem().getId()
            ));
        }

        EstoqueDTO estoque = new EstoqueDTO(banco.getId(), bolsasDTO.size(), bolsasDTO);
        return ResponseEntity.ok(estoque);
    }
}