package com.rotavital.servico;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.estoque.ResumoEstoqueDTO;
import com.rotavital.api.dto.estoque.TotalPorComponenteDTO;
import com.rotavital.api.dto.estoque.TotalPorTipoSanguineoDTO;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.ResumoEstoqueRepository;
import com.rotavital.repositorio.ResumoEstoqueRepository.TotalPorTipo;
import com.rotavital.repositorio.ResumoEstoqueRepository.TotalPorTipoEComponente;

@Service
public class ResumoEstoqueService
{
    private final ResumoEstoqueRepository resumos;

    public ResumoEstoqueService(ResumoEstoqueRepository resumos)
    {
        this.resumos = resumos;
    }

    /**
     * Totais de bolsas disponíveis e dentro da validade de um banco de sangue: por tipo sanguíneo
     * (com a divisão por componente) e por componente. Todos os tipos e componentes aparecem,
     * inclusive os zerados, sempre na ordem dos enums. Vazio quando o banco não existe.
     */
    @Transactional(readOnly = true)
    public Optional<ResumoEstoqueDTO> resumir(String bancoId)
    {
        List<TotalPorTipo> totaisPorTipo = resumos.totaisPorTipo(bancoId);

        // A view devolve os 8 tipos para todo banco cadastrado: lista vazia quer dizer banco inexistente.
        if (totaisPorTipo.isEmpty())
        {
            return Optional.empty();
        }

        // A segunda consulta só traz as combinações com bolsa; o que faltar aqui vira zero na resposta.
        Map<TipoSanguineo, Map<TipoComponente, Integer>> unidadesPorTipo = new EnumMap<>(TipoSanguineo.class);
        Map<TipoComponente, Integer> unidadesPorComponente = new EnumMap<>(TipoComponente.class);

        for (TotalPorTipoEComponente linha : resumos.totaisPorTipoEComponente(bancoId))
        {
            unidadesPorTipo.computeIfAbsent(linha.getTipoSanguineo(), tipo -> new EnumMap<>(TipoComponente.class))
                    .put(linha.getTipoComponente(), linha.getUnidades());
            unidadesPorComponente.merge(linha.getTipoComponente(), linha.getUnidades(), Integer::sum);
        }

        List<TotalPorTipoSanguineoDTO> tiposSanguineos = new ArrayList<>();
        int totalUnidades = 0;

        for (TotalPorTipo total : totaisPorTipo)
        {
            Map<TipoComponente, Integer> componentes = unidadesPorTipo.getOrDefault(total.getTipoSanguineo(), Map.of());

            tiposSanguineos.add(new TotalPorTipoSanguineoDTO(
                    total.getTipoSanguineo(),
                    total.getUnidades(),
                    total.isAbaixoNivelSeguranca(),
                    listarComponentes(componentes)
            ));
            totalUnidades += total.getUnidades();
        }

        // A view não garante a ordem das linhas: a ordenação é feita aqui, pela ordem do enum.
        tiposSanguineos.sort(Comparator.comparing(TotalPorTipoSanguineoDTO::tipoSanguineo));

        return Optional.of(new ResumoEstoqueDTO(bancoId, totalUnidades, tiposSanguineos,
                listarComponentes(unidadesPorComponente)));
    }

    // Os quatro componentes na ordem do enum, com zero para os que não têm bolsa.
    private static List<TotalPorComponenteDTO> listarComponentes(Map<TipoComponente, Integer> unidades)
    {
        List<TotalPorComponenteDTO> componentes = new ArrayList<>();

        for (TipoComponente componente : TipoComponente.values())
        {
            componentes.add(new TotalPorComponenteDTO(componente, unidades.getOrDefault(componente, 0)));
        }

        return componentes;
    }
}
