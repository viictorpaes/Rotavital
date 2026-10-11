package com.rotavital.servico;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.api.dto.estoque.NovaBolsaHemocomponenteRequest;
import com.rotavital.dominio.BancoDeSangue;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.repositorio.BancoDeSangueRepository;
import com.rotavital.repositorio.BolsaHemocomponenteRepository;

// Validação dos dados de uma bolsa nova antes do cadastro: campo ausente ou mal preenchido é 400
// (DadosInvalidosException) e regra do estoque quebrada é 422 (RegraDeNegocioException).
public class ValidacaoNovaBolsaTest
{
    private final LocalDate hoje = LocalDate.of(2026, 10, 1);
    private EstoqueService servico;

    @BeforeEach
    public void criarServicoComOBancoBS01()
    {
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", new Endereco("Recife/PE", -8.0578, -34.8829));

        // Só o BS-01 existe; a validação não consulta bolsas.
        BancoDeSangueRepository bancosFalsos = RepositorioFalso.respondendo(BancoDeSangueRepository.class,
                "findById", argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? banco : null));
        BolsaHemocomponenteRepository semBolsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class,
                "findById", argumentos -> Optional.empty());
        servico = new EstoqueService(bancosFalsos, semBolsas);
    }

    private NovaBolsaHemocomponenteRequest bolsa(LocalDate dataColeta, LocalDate dataValidade, String bancoOrigemId)
    {
        return new NovaBolsaHemocomponenteRequest(TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                dataColeta, dataValidade, "LOTE-SIM-0001", 450.0, bancoOrigemId);
    }

    private NovaBolsaHemocomponenteRequest bolsaValida()
    {
        return bolsa(hoje.minusDays(5), hoje.plusDays(30), "BS-01");
    }

    @Test
    public void dadosCompletosPassam()
    {
        Assertions.assertDoesNotThrow(() -> servico.validarNovaBolsa(bolsaValida(), hoje));
    }

    @Test
    public void bolsaQueVenceHojeAindaEntra()
    {
        Assertions.assertDoesNotThrow(() -> servico.validarNovaBolsa(bolsa(hoje.minusDays(30), hoje, "BS-01"), hoje));
    }

    @Test
    public void listaTodosOsCamposObrigatoriosQueFaltam()
    {
        // Corpo vazio ({}): os objetos chegam null e o volume chega 0.
        NovaBolsaHemocomponenteRequest vazia = new NovaBolsaHemocomponenteRequest(null, null, null, null, null, 0.0, null);

        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.validarNovaBolsa(vazia, hoje));

        Assertions.assertEquals("O campo 'tipoComponente' é obrigatório. "
                + "O campo 'tipoSanguineo' é obrigatório. "
                + "O campo 'dataColeta' é obrigatório. "
                + "O campo 'dataValidade' é obrigatório. "
                + "O campo 'loteSintetico' é obrigatório. "
                + "O campo 'volumeMl' é obrigatório e deve ser maior que zero. "
                + "O campo 'bancoOrigemId' é obrigatório.", erro.getMessage());
    }

    @Test
    public void loteEmBrancoEVolumeNegativoSaoInvalidos()
    {
        NovaBolsaHemocomponenteRequest dados = new NovaBolsaHemocomponenteRequest(TipoComponente.PLASMA,
                TipoSanguineo.A_NEGATIVO, hoje.minusDays(5), hoje.plusDays(300), "   ", -10.0, "BS-01");

        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.validarNovaBolsa(dados, hoje));

        Assertions.assertEquals("O campo 'loteSintetico' é obrigatório. "
                + "O campo 'volumeMl' é obrigatório e deve ser maior que zero.", erro.getMessage());
    }

    @Test
    public void preenchimentoEConferidoAntesDasRegras()
    {
        // Banco inexistente seria 422, mas o campo faltando vem primeiro: corrige-se um passo por vez.
        NovaBolsaHemocomponenteRequest dados = new NovaBolsaHemocomponenteRequest(null, TipoSanguineo.O_POSITIVO,
                hoje.minusDays(5), hoje.plusDays(30), "LOTE-SIM-0001", 450.0, "BS-99");

        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.validarNovaBolsa(dados, hoje));

        Assertions.assertEquals("O campo 'tipoComponente' é obrigatório.", erro.getMessage());
    }

    @Test
    public void validadeAntesDaColetaQuebraARegra()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(bolsa(hoje.minusDays(5), hoje.minusDays(11), "BS-01"), hoje));

        Assertions.assertEquals("A data de validade (20/09/2026) não pode ser anterior à data de coleta (26/09/2026).",
                erro.getMessage());
    }

    @Test
    public void coletaDepoisDeHojeQuebraARegra()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(bolsa(hoje.plusDays(4), hoje.plusDays(40), "BS-01"), hoje));

        Assertions.assertEquals("A data de coleta (05/10/2026) não pode ser depois de hoje (01/10/2026).",
                erro.getMessage());
    }

    @Test
    public void bolsaJaVencidaNaoEntraNoEstoque()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(bolsa(hoje.minusDays(30), hoje.minusDays(1), "BS-01"), hoje));

        Assertions.assertEquals("A bolsa venceu em 30/09/2026 e não pode entrar no estoque.", erro.getMessage());
    }

    @Test
    public void bancoDeOrigemInexistenteQuebraARegra()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(bolsa(hoje.minusDays(5), hoje.plusDays(30), "BS-99"), hoje));

        Assertions.assertEquals("Nenhum banco de sangue encontrado com id BS-99.", erro.getMessage());
    }

    @Test
    public void juntaTodasAsRegrasQuebradasNaMesmaMensagem()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(bolsa(hoje.plusDays(4), hoje.plusDays(40), "BS-99"), hoje));

        Assertions.assertEquals("A data de coleta (05/10/2026) não pode ser depois de hoje (01/10/2026). "
                + "Nenhum banco de sangue encontrado com id BS-99.", erro.getMessage());
    }
}
