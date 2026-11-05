package com.rotavital.repositorio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rotavital.dominio.StatusTransferencia;
import com.rotavital.dominio.TransferenciaDeBolsa;

public interface TransferenciaDeBolsaRepository extends JpaRepository<TransferenciaDeBolsa, UUID>
{
    // Da que saiu primeiro para a última: é a primeira da fila a chegar.
    List<TransferenciaDeBolsa> findByStatusOrderByDespachadaEmAsc(StatusTransferencia status);

    List<TransferenciaDeBolsa> findAllByOrderByDespachadaEmDesc();

    Optional<TransferenciaDeBolsa> findByBolsaIdAndStatus(String bolsaId, StatusTransferencia status);
}
