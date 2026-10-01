package com.rotavital.servico;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.dominio.BolsaHemocomponente;
import com.rotavital.dominio.Estoque;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;

@Service
public class EstoqueService
{
    private final BancoDeSangueRepository bancos;

    public EstoqueService(BancoDeSangueRepository bancos)
    {
        this.bancos = bancos;
    }

    /**
     * Bolsas do estoque de um banco de sangue, ordenadas por validade.
     * Vazio quando o banco não existe.
     *
     * A lista de bolsas é carregada do banco de dados só quando é acessada, e o projeto usa
     * open-in-view=false: por isso a leitura acontece aqui, dentro da transação.
     */
    @Transactional(readOnly = true)
    public Optional<List<BolsaHemocomponente>> buscarBolsas(String bancoId, TipoSanguineo tipoSanguineo)
    {
        return bancos.findById(bancoId).map(banco ->
        {
            Estoque estoque = banco.getEstoque();

            if (tipoSanguineo == null)
            {
                return new ArrayList<>(estoque.getBolsas());
            }

            return estoque.buscarPorTipoSanguineo(tipoSanguineo);
        });
    }
}
