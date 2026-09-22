package com.rotavital.benchmark;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.rotavital.benchmark.model.RegistroTelemetria;
import com.rotavital.benchmark.model.ResultadoAuditoria;
import com.rotavital.benchmark.service.AuditoriaTelemetriaService;

public class AuditoriaTelemetriaTest {

    @Test
    public void testarConcorrenciaEConsistenciaDeResultados() throws Exception {
        AuditoriaTelemetriaService service = new AuditoriaTelemetriaService();
        List<RegistroTelemetria> dados = service.gerarMassaDados(50000);

        ResultadoAuditoria seq = service.processarSequencial(dados);
        ResultadoAuditoria t2 = service.processarComThreads(dados, 2);
        ResultadoAuditoria t4 = service.processarComThreads(dados, 4);
        ResultadoAuditoria t8 = service.processarComThreads(dados, 8);
        ResultadoAuditoria vt = service.processarComVirtualThreads(dados, 8);

        // Verifica que todos processaram exatamente a mesma quantidade
        Assertions.assertEquals(50000, seq.getTotalProcessado());
        Assertions.assertEquals(seq.getTotalProcessado(), t2.getTotalProcessado());
        Assertions.assertEquals(seq.getTotalProcessado(), t4.getTotalProcessado());
        Assertions.assertEquals(seq.getTotalProcessado(), t8.getTotalProcessado());
        Assertions.assertEquals(seq.getTotalProcessado(), vt.getTotalProcessado());

        // Verifica consistência estrita dos cálculos (Zero Race Condition)
        Assertions.assertEquals(seq.getTotalAnomalias(), t2.getTotalAnomalias());
        Assertions.assertEquals(seq.getTotalAnomalias(), t4.getTotalAnomalias());
        Assertions.assertEquals(seq.getTotalAnomalias(), t8.getTotalAnomalias());
        Assertions.assertEquals(seq.getTotalAnomalias(), vt.getTotalAnomalias());

        Assertions.assertEquals(seq.getTotalDescartesCriticos(), t4.getTotalDescartesCriticos());
        Assertions.assertEquals(seq.getMediaTemperatura(), t8.getMediaTemperatura(), 0.001);
        Assertions.assertEquals(seq.getDesvioPadraoTemperatura(), vt.getDesvioPadraoTemperatura(), 0.001);
    }
}

