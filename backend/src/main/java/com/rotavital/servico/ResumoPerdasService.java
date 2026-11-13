package com.rotavital.servico;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.estoque.ResumoPerdasDTO;
import com.rotavital.api.dto.estoque.TotalPerdaDTO;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository.TotalPerda;

@Service
public class ResumoPerdasService
{
    private final BancoDeSangueRepository bancos;
    private final BolsaHemocomponenteRepository bolsas;

    public ResumoPerdasService(BancoDeSangueRepository bancos, BolsaHemocomponenteRepository bolsas)
    {
        this.bancos = bancos;
        this.bolsas = bolsas;
    }

    @Transactional(readOnly = true)
    public Optional<ResumoPerdasDTO> resumir(String bancoId)
    {
        if (!bancos.existsById(bancoId))
        {
            return Optional.empty();
        }

        List<TotalPerdaDTO> porTipoSanguineo = converter(bolsas.perdasPorTipoSanguineo(bancoId));
        List<TotalPerdaDTO> porComponente = converter(bolsas.perdasPorComponente(bancoId));
        long totalBolsas = porTipoSanguineo.stream().mapToLong(TotalPerdaDTO::bolsas).sum();
        double totalVolumeMl = porTipoSanguineo.stream().mapToDouble(TotalPerdaDTO::volumeMl).sum();

        return Optional.of(new ResumoPerdasDTO(totalBolsas, totalVolumeMl, porTipoSanguineo, porComponente));
    }

    private static List<TotalPerdaDTO> converter(List<TotalPerda> totais)
    {
        return totais.stream()
                .map(total -> new TotalPerdaDTO(total.getCategoria(), total.getBolsas(), total.getVolumeMl()))
                .toList();
    }
}
