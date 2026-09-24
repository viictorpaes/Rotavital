package com.rotavital.api;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.RedeDistribuicao;

@Component
public class RedeDistribuicaoEmMemoria
{
    private final RedeDistribuicao rede;

    public RedeDistribuicaoEmMemoria(BancosEmMemoria bancos)
    {
        rede = new RedeDistribuicao();

        BancoDeSangue hemope = bancos.buscarPorId("BS-01");
        rede.adicionarPonto(hemope);

        Hospital hospitalDasClinicas = new Hospital("HOSP-01", "Hospital das Clinicas",
                new Endereco("Rua das Flores, 500 - Recife/PE", -8.0476, -34.8770));

        Hospital hcPe = new Hospital("hc-pe", "Hospital das Clínicas de PE",
                new Endereco("Av. Prof. Moraes Rego, 1235 - Cidade Universitária, Recife/PE", -8.0512, -34.9478));

        Hospital realPortugues = new Hospital("real-portugues", "Real Hospital Português",
                new Endereco("Av. Gov. Agamenon Magalhães, 4760 - Paissandu, Recife/PE", -8.0479, -34.8993));

        Hospital baraoLucena = new Hospital("barao-lucena", "Hospital Barão de Lucena",
                new Endereco("Av. Caxangá, 3860 - Iputinga, Recife/PE", -8.0432, -34.9331));

        Hospital getulioVargas = new Hospital("getulio-vargas", "Hospital Getúlio Vargas",
                new Endereco("R. Cons. Portela, 1034 - Afogados, Recife/PE", -8.0812, -34.9127));

        Hospital upaNorte = new Hospital("upa-norte", "UPA Norte - Macaxeira",
                new Endereco("Av. Norte Miguel Arraes, 7200 - Macaxeira, Recife/PE", -8.0098, -34.9296));

        List<Hospital> hospitais = List.of(hospitalDasClinicas, hcPe, 
                realPortugues, baraoLucena, getulioVargas, upaNorte);
        hospitais.forEach(rede::adicionarPonto);

        // Topologia em estrela a partir do hemocentro — distância/tempo sintéticos (sem roteador real aqui),
        // mesma convenção de dado simulado usada em BancosEmMemoria e na telemetria.
        rede.adicionarConexao(hemope, hospitalDasClinicas, 3.2, 9);
        rede.adicionarConexao(hemope, hcPe, 7.8, 18);
        rede.adicionarConexao(hemope, realPortugues, 2.1, 7);
        rede.adicionarConexao(hemope, baraoLucena, 1.9, 6);
        rede.adicionarConexao(hemope, getulioVargas, 7.1, 16);
        rede.adicionarConexao(hemope, upaNorte, 8.4, 20);
    }

    public RedeDistribuicao getRede()
    {
        return rede;
    }
}