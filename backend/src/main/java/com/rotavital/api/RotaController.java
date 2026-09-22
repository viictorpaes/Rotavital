package com.rotavital.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.api.dto.rota.CalcularRotaRequest;
import com.rotavital.api.dto.rota.ConexaoDTO;
import com.rotavital.api.dto.rota.PontoRedeDTO;
import com.rotavital.api.dto.rota.RotaCalculadaDTO;
import com.rotavital.api.dto.rota.TipoPontoRede;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.Conexao;
import com.rotavital.dominio.PontoDeRede;
import com.rotavital.dominio.RedeDistribuicao;
import com.rotavital.dominio.RotaCalculada;

@RestController
public class RotaController
{
    private final RedeDistribuicao rede;

    public RotaController(RedeDistribuicaoEmMemoria redeDistribuicaoEmMemoria)
    {
        this.rede = redeDistribuicaoEmMemoria.getRede();
    }

    @GetMapping("/rotas/pontos")
    public ResponseEntity<List<PontoRedeDTO>> listarPontos()
    {
        List<PontoRedeDTO> pontos = rede.getPontos().stream()
                .map(this::paraPontoRedeDTO)
                .toList();

        return ResponseEntity.ok(pontos);
    }

    @GetMapping("/rotas/conexoes")
    public ResponseEntity<List<ConexaoDTO>> listarConexoes()
    {
        List<ConexaoDTO> conexoes = rede.getConexoes().stream()
                .map(this::paraConexaoDTO)
                .toList();

        return ResponseEntity.ok(conexoes);
    }

    @PostMapping("/rotas/calcular")
    public ResponseEntity<?> calcularRota(@RequestBody CalcularRotaRequest request)
    {
        try
        {
            RotaCalculada rota = rede.calcularRotaMinima(
                    request.origemId(), request.destinoId(), request.janelaEntregaLimite());

            List<String> nos = rota.getNos().stream().map(PontoDeRede::getId).toList();

            RotaCalculadaDTO dto = new RotaCalculadaDTO(
                    rota.getOrigem().getId(),
                    rota.getDestino().getId(),
                    nos,
                    rota.getDistanciaTotalKm(),
                    rota.getTempoEstimadoMin(),
                    rota.isDentroDaJanela());

            return ResponseEntity.ok(dto);
        }
        catch (IllegalArgumentException e)
        {
            ErroDTO erro = new ErroDTO(null, "Recurso não encontrado", 404, e.getMessage(), "/rotas/calcular");
            return ResponseEntity.status(404).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(erro);
        }
        catch (IllegalStateException e)
        {
            ErroDTO erro = new ErroDTO(null, "Regra de negócio violada", 422, e.getMessage(), "/rotas/calcular");
            return ResponseEntity.status(422).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(erro);
        }
    }

    private PontoRedeDTO paraPontoRedeDTO(PontoDeRede ponto)
    {
        TipoPontoRede tipo = ponto instanceof BancoDeSangue ? TipoPontoRede.BANCO_DE_SANGUE : TipoPontoRede.HOSPITAL;
        return new PontoRedeDTO(ponto.getId(), ponto.getNome(), tipo, ponto.getLatitude(), ponto.getLongitude());
    }

    private ConexaoDTO paraConexaoDTO(Conexao conexao)
    {
        return new ConexaoDTO(conexao.getOrigem().getId(), conexao.getDestino().getId(),
                conexao.getDistanciaKm(), conexao.getTempoEstimadoMin());
    }
}