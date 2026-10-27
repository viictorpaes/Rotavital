package com.rotavital.repositorio;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rotavital.dominio.AgendamentoDoacao;
import com.rotavital.dominio.StatusAgendamento;

// Consultas na tabela agendamento_doacao, do horário mais próximo para o mais distante.
public interface AgendamentoDoacaoRepository extends JpaRepository<AgendamentoDoacao, UUID>
{
    List<AgendamentoDoacao> findByCampanhaIdOrderByAgendadoParaAsc(UUID campanhaId);

    List<AgendamentoDoacao> findByCampanhaIdAndStatus(UUID campanhaId, StatusAgendamento status);

    List<AgendamentoDoacao> findByDoadorIdOrderByAgendadoParaDesc(UUID doadorId);

    // Uma linha por agendamento em aberto, com o id da campanha: a lista de campanhas conta quantos doadores vêm.
    @Query("select a.campanha.id from AgendamentoDoacao a "
            + "where a.status = com.rotavital.dominio.StatusAgendamento.AGENDADO and a.campanha is not null")
    List<UUID> campanhasDosAgendamentosEmAberto();

    boolean existsByDoadorIdAndStatus(UUID doadorId, StatusAgendamento status);
}
