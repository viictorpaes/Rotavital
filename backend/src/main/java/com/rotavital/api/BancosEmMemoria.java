package com.rotavital.api;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.Estoque;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;

@Component
public class BancosEmMemoria
{
    private final Map<String, BancoDeSangue> bancos = new HashMap<>();

    public BancosEmMemoria()
    {
        Endereco endereco = new Endereco("Av. Central, 100 - Recife/PE", -8.0578, -34.8829);
        BancoDeSangue hemope = new BancoDeSangue("BS-01", "Hemope Central", endereco);
        Estoque estoque = hemope.getEstoque();
        LocalDate hoje = LocalDate.now();

        estoque.adicionarBolsa(new BolsaHemocomponente("CH-1042", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                hoje.minusDays(32), hoje.plusDays(3), "LOTE-SIM-0001", 450.0, 4.0, "R1 · P3 · N2", hemope));
        estoque.adicionarBolsa(new BolsaHemocomponente("CH-1043", TipoComponente.HEMACIAS, TipoSanguineo.O_NEGATIVO,
                hoje.minusDays(15), hoje.plusDays(20), "LOTE-SIM-0002", 450.0, 3.5, "R1 · P3 · N3", hemope));
        estoque.adicionarBolsa(new BolsaHemocomponente("CR-4002", TipoComponente.CRIOPRECIPITADO, TipoSanguineo.O_NEGATIVO,
                hoje.minusDays(10), hoje.plusDays(355), "LOTE-SIM-0003", 30.0, -25.0, "F2 · P1 · N1", hemope));
        estoque.adicionarBolsa(new BolsaHemocomponente("PQ-3014", TipoComponente.PLAQUETAS, TipoSanguineo.A_POSITIVO,
                hoje.minusDays(2), hoje.plusDays(3), "LOTE-SIM-0004", 300.0, 25.6, "R3 · P2 · N1", hemope));
        estoque.adicionarBolsa(new BolsaHemocomponente("CH-1080", TipoComponente.HEMACIAS, TipoSanguineo.AB_POSITIVO,
                hoje.minusDays(29), hoje.plusDays(6), "LOTE-SIM-0005", 450.0, 5.0, "R2 · P1 · N1", hemope));
        estoque.adicionarBolsa(new BolsaHemocomponente("CH-1061", TipoComponente.HEMACIAS, TipoSanguineo.A_NEGATIVO,
                hoje.minusDays(26), hoje.plusDays(9), "LOTE-SIM-0006", 450.0, 4.5, "R2 · P2 · N1", hemope));

        bancos.put(hemope.getId(), hemope);
    }

    public BancoDeSangue buscarPorId(String id)
    {
        return bancos.get(id);
    }
}
