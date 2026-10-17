package com.rotavital.servico;

import java.time.LocalDate;
import java.util.Map;
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

        // Só o BS-01 existe. Das bolsas, a validação só pergunta se o lote já foi usado no mesmo
        // componente: aqui, só o LOTE-REPETIDO de hemácias.
        BancoDeSangueRepository bancosFalsos = RepositorioFalso.respondendo(BancoDeSangueRepository.class,
                "findById", argumentos -> Optional.ofNullable("BS-01".equals(argumentos[0]) ? banco : null));
        BolsaHemocomponenteRepository bolsasFalsas = RepositorioFalso.respondendo(BolsaHemocomponenteRepository.class,
                Map.of("existsByLoteSinteticoAndTipoComponente", argumentos ->
                        "LOTE-REPETIDO".equals(argumentos[0]) && argumentos[1] == TipoComponente.HEMACIAS));
        servico = new EstoqueService(bancosFalsos, bolsasFalsas);
    }

    private NovaBolsaHemocomponenteRequest bolsa(LocalDate dataColeta, LocalDate dataValidade, String bancoOrigemId)
    {
        return new NovaBolsaHemocomponenteRequest(TipoComponente.HEMACIAS, TipoSanguineo.O_POSITIVO,
                dataColeta, dataValidade, "LOTE-SIM-0001", 450.0, 4.0, "R1 · P1 · N1", bancoOrigemId);
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
        NovaBolsaHemocomponenteRequest vazia = new NovaBolsaHemocomponenteRequest(null, null, null, null, null, 0.0,
                null, null, null);

        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.validarNovaBolsa(vazia, hoje));

        Assertions.assertEquals("O campo 'tipoComponente' é obrigatório. "
                + "O campo 'tipoSanguineo' é obrigatório. "
                + "O campo 'dataColeta' é obrigatório. "
                + "O campo 'dataValidade' é obrigatório. "
                + "O campo 'loteSintetico' é obrigatório. "
                + "O campo 'volumeMl' é obrigatório e deve ser maior que zero. "
                + "O campo 'temperaturaCelsius' é obrigatório. "
                + "O campo 'bancoOrigemId' é obrigatório.", erro.getMessage());
    }

    @Test
    public void loteEmBrancoEVolumeNegativoSaoInvalidos()
    {
        NovaBolsaHemocomponenteRequest dados = new NovaBolsaHemocomponenteRequest(TipoComponente.PLASMA,
                TipoSanguineo.A_NEGATIVO, hoje.minusDays(5), hoje.plusDays(300), "   ", -10.0, -25.0, null, "BS-01");

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
                hoje.minusDays(5), hoje.plusDays(30), "LOTE-SIM-0001", 450.0, 4.0, null, "BS-99");

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

    // A partir daqui, as regras que o banco de dados também confere: sem elas, a bolsa passaria pela
    // validação, o banco recusaria a gravação e a API responderia 500 em vez de 422.

    private NovaBolsaHemocomponenteRequest bolsa(TipoComponente componente, LocalDate dataColeta, LocalDate dataValidade,
    String lote, double volumeMl, Double temperaturaCelsius)
    {
        return new NovaBolsaHemocomponenteRequest(componente, TipoSanguineo.O_POSITIVO, dataColeta, dataValidade,
                lote, volumeMl, temperaturaCelsius, null, "BS-01");
    }

    @Test
    public void localizacaoPodeFicarDeFora()
    {
        NovaBolsaHemocomponenteRequest dados = bolsa(TipoComponente.HEMACIAS, hoje.minusDays(5), hoje.plusDays(30),
                "LOTE-SIM-0001", 450.0, 4.0);

        Assertions.assertDoesNotThrow(() -> servico.validarNovaBolsa(dados, hoje));
    }

    @Test
    public void validadeNoMesmoDiaDaColetaQuebraARegra()
    {
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(bolsa(hoje, hoje, "BS-01"), hoje));

        Assertions.assertEquals("A data de validade (01/10/2026) precisa ser depois da data de coleta.", erro.getMessage());
    }

    @Test
    public void validadeAlemDoPrazoDoComponenteQuebraARegra()
    {
        // Hemácias duram no máximo 42 dias depois da coleta.
        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(bolsa(hoje.minusDays(5), hoje.plusDays(40), "BS-01"), hoje));

        Assertions.assertEquals("A validade de HEMACIAS é de no máximo 42 dias depois da coleta, "
                + "e foram informados 45 dias.", erro.getMessage());
    }

    @Test
    public void validadeNoLimiteDoPrazoAindaEntra()
    {
        // Plaquetas: 7 dias é o máximo e ainda vale.
        NovaBolsaHemocomponenteRequest dados = bolsa(TipoComponente.PLAQUETAS, hoje.minusDays(2), hoje.plusDays(5),
                "LOTE-SIM-0001", 50.0, 22.0);

        Assertions.assertDoesNotThrow(() -> servico.validarNovaBolsa(dados, hoje));
    }

    @Test
    public void volumeAcimaDoMaximoQuebraARegra()
    {
        NovaBolsaHemocomponenteRequest dados = bolsa(TipoComponente.HEMACIAS, hoje.minusDays(5), hoje.plusDays(30),
                "LOTE-SIM-0001", 800.0, 4.0);

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(dados, hoje));

        Assertions.assertEquals("O volume (800.0 ml) passa do máximo de 700.0 ml por bolsa.", erro.getMessage());
    }

    @Test
    public void temperaturaForaDoQueOSensorMedeQuebraARegra()
    {
        NovaBolsaHemocomponenteRequest dados = bolsa(TipoComponente.HEMACIAS, hoje.minusDays(5), hoje.plusDays(30),
                "LOTE-SIM-0001", 450.0, 90.0);

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(dados, hoje));

        Assertions.assertEquals("A temperatura (90.0 °C) está fora do que o sensor mede (-80.0 °C a 60.0 °C).",
                erro.getMessage());
    }

    @Test
    public void temperaturaForaDaFaixaIdealAindaEntra()
    {
        // 10 °C é quente para hemácias (2 a 6 °C), mas isso é alerta na tela, não dado inválido.
        NovaBolsaHemocomponenteRequest dados = bolsa(TipoComponente.HEMACIAS, hoje.minusDays(5), hoje.plusDays(30),
                "LOTE-SIM-0001", 450.0, 10.0);

        Assertions.assertDoesNotThrow(() -> servico.validarNovaBolsa(dados, hoje));
    }

    @Test
    public void loteJaUsadoNoMesmoComponenteQuebraARegra()
    {
        NovaBolsaHemocomponenteRequest dados = bolsa(TipoComponente.HEMACIAS, hoje.minusDays(5), hoje.plusDays(30),
                "LOTE-REPETIDO", 450.0, 4.0);

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.validarNovaBolsa(dados, hoje));

        Assertions.assertEquals("Já existe uma bolsa de HEMACIAS do lote LOTE-REPETIDO: "
                + "cada doação gera no máximo uma bolsa de cada componente.", erro.getMessage());
    }

    @Test
    public void mesmoLoteEmOutroComponenteEntra()
    {
        // A mesma doação pode gerar uma bolsa de hemácias e outra de plasma.
        NovaBolsaHemocomponenteRequest dados = bolsa(TipoComponente.PLASMA, hoje.minusDays(5), hoje.plusDays(300),
                "LOTE-REPETIDO", 200.0, -25.0);

        Assertions.assertDoesNotThrow(() -> servico.validarNovaBolsa(dados, hoje));
    }
}
