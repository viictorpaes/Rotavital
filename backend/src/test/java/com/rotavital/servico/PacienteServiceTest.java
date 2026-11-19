package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.api.dto.paciente.PacienteRequest;
import com.rotavital.config.RelogioConfig;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.Sexo;
import com.rotavital.dominio.StatusPaciente;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.HospitalRepository;
import com.rotavital.repositorio.PacienteRepository;
import com.rotavital.repositorio.UsuarioRepository;

// Cadastro de pacientes (PI3-194): quem pode gravar, a ordem da fila, a edição e a remoção.
public class PacienteServiceTest
{
    // Quinta-feira, 29/10/2026, 10h em Recife.
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 10, 29, 10, 0, 0, 0, ZoneOffset.ofHours(-3));

    private final Usuario medico = new Usuario(UUID.randomUUID(), null, "Dra. Ana Lima", PapelUsuario.MEDICO, "HS-01", null);
    private final Usuario admin = new Usuario(UUID.randomUUID(), null, "Admin", PapelUsuario.ADMIN, null, null);
    private final Hospital hospital = new Hospital("HS-01", "Hospital da Restauração", new Endereco("Recife/PE", -8.05, -34.89));

    private final List<Paciente> gravados = new ArrayList<>();
    private final List<Paciente> removidos = new ArrayList<>();
    private final List<Paciente> naTabela = new ArrayList<>();
    private final Map<String, Long> vinculos = new java.util.HashMap<>();
    private Object[] filtrosUsados;
    private PacienteService servico;

    @BeforeEach
    public void montarServico()
    {
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(medico.getId().equals(argumentos[0]) ? medico
                        : admin.getId().equals(argumentos[0]) ? admin : null));
        HospitalRepository hospitais = RepositorioFalso.respondendo(HospitalRepository.class, "findById",
                argumentos -> Optional.ofNullable("HS-01".equals(argumentos[0]) ? hospital : null));
        PacienteRepository pacientes = RepositorioFalso.respondendo(PacienteRepository.class, Map.of(
                "save", argumentos ->
                {
                    gravados.add((Paciente) argumentos[0]);
                    return argumentos[0];
                },
                "delete", argumentos ->
                {
                    removidos.add((Paciente) argumentos[0]);
                    return null;
                },
                "findById", argumentos -> naTabela.stream().findFirst(),
                "buscarComFiltros", argumentos ->
                {
                    filtrosUsados = argumentos;
                    return naTabela;
                },
                "contarCampanhas", argumentos -> vinculos.getOrDefault("campanhas", 0L),
                "contarProcedimentos", argumentos -> vinculos.getOrDefault("procedimentos", 0L),
                "contarRequisicoes", argumentos -> vinculos.getOrDefault("requisicoes", 0L)));

        servico = new PacienteService(pacientes, hospitais, new IdentificacaoService(usuarios),
                Clock.fixed(AGORA.toInstant(), RelogioConfig.FUSO));
    }

    private static PacienteRequest pedidoValido()
    {
        return new PacienteRequest("HS-01", "  Carlos Lima ", Sexo.MASCULINO, LocalDate.of(1980, 1, 15),
                TipoSanguineo.A_POSITIVO, TipoComponente.PLAQUETAS, 3, " Leucemia ", Gravidade.ATENCAO);
    }

    private Paciente paciente(String nome, Gravidade gravidade, OffsetDateTime criadoEm)
    {
        return new Paciente(hospital, nome, Sexo.FEMININO, LocalDate.of(1990, 5, 10), TipoSanguineo.O_NEGATIVO,
                TipoComponente.HEMACIAS, 2, "Cirurgia cardíaca", gravidade, criadoEm);
    }

    @Test
    public void medicoCadastraPacienteAguardando()
    {
        Paciente paciente = servico.cadastrar(medico.getId(), pedidoValido());

        Assertions.assertEquals("Carlos Lima", paciente.getNome());
        Assertions.assertEquals("Leucemia", paciente.getCausa());
        Assertions.assertEquals(StatusPaciente.AGUARDANDO, paciente.getStatus());
        Assertions.assertEquals(AGORA, paciente.getCriadoEm());
        Assertions.assertSame(hospital, paciente.getHospital());
        Assertions.assertEquals(List.of(paciente), gravados);
    }

    @Test
    public void semLoginNaoCadastra()
    {
        UsuarioNaoIdentificadoException erro = Assertions.assertThrows(UsuarioNaoIdentificadoException.class,
                () -> servico.cadastrar(null, pedidoValido()));

        Assertions.assertEquals("Entre no sistema para cadastrar paciente.", erro.getMessage());
        Assertions.assertTrue(gravados.isEmpty());
    }

    @Test
    public void adminSoConsulta()
    {
        AcessoNegadoException erro = Assertions.assertThrows(AcessoNegadoException.class,
                () -> servico.cadastrar(admin.getId(), pedidoValido()));

        Assertions.assertEquals("Só médico pode cadastrar paciente.", erro.getMessage());
    }

    @Test
    public void dadosInvalidosJuntamTodosOsProblemas()
    {
        PacienteRequest ruim = new PacienteRequest("HS-99", " ", null, AGORA.toLocalDate().plusDays(1),
                TipoSanguineo.A_POSITIVO, TipoComponente.PLAQUETAS, 60, "", Gravidade.ATENCAO);

        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.cadastrar(medico.getId(), ruim));

        Assertions.assertEquals("O campo 'nome' é obrigatório. O campo 'sexo' é obrigatório. A data de nascimento "
                + "(30/10/2026) não pode ser depois de hoje (29/10/2026). O campo 'unidadesNecessarias' deve ficar "
                + "entre 1 e 50. O campo 'causa' é obrigatório. Nenhum hospital encontrado com id HS-99.",
                erro.getMessage());
        Assertions.assertTrue(gravados.isEmpty());
    }

    @Test
    public void listaNaOrdemDaFilaClinica()
    {
        Paciente estavelAntigo = paciente("Estável antigo", Gravidade.ESTAVEL, AGORA.minusDays(9));
        Paciente criticoNovo = paciente("Crítico novo", Gravidade.CRITICO, AGORA.minusHours(1));
        Paciente criticoAntigo = paciente("Crítico antigo", Gravidade.CRITICO, AGORA.minusDays(2));
        Paciente atencao = paciente("Atenção", Gravidade.ATENCAO, AGORA.minusDays(5));
        naTabela.addAll(List.of(estavelAntigo, criticoNovo, criticoAntigo, atencao));

        List<Paciente> fila = servico.listar(StatusPaciente.AGUARDANDO, null, null, "  ");

        Assertions.assertEquals(List.of(criticoAntigo, criticoNovo, atencao, estavelAntigo), fila);
        // Hospital em branco não filtra.
        Assertions.assertArrayEquals(new Object[] { StatusPaciente.AGUARDANDO, null, null, null }, filtrosUsados);
    }

    @Test
    public void pacienteInexistenteDa404()
    {
        UUID id = UUID.randomUUID();
        RecursoNaoEncontradoException erro = Assertions.assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.buscar(id));

        Assertions.assertEquals("Nenhum paciente encontrado com id " + id + ".", erro.getMessage());
    }

    @Test
    public void edicaoTrocaOsDadosEMantemStatusECriacao()
    {
        Paciente existente = paciente("Maria Souza", Gravidade.CRITICO, AGORA.minusDays(3));
        naTabela.add(existente);

        Paciente editado = servico.editar(medico.getId(), UUID.randomUUID(), pedidoValido());

        Assertions.assertSame(existente, editado);
        Assertions.assertEquals("Carlos Lima", editado.getNome());
        Assertions.assertEquals(Gravidade.ATENCAO, editado.getGravidade());
        Assertions.assertEquals(3, editado.getUnidadesNecessarias());
        Assertions.assertEquals(StatusPaciente.AGUARDANDO, editado.getStatus());
        Assertions.assertEquals(AGORA.minusDays(3), editado.getCriadoEm());
        Assertions.assertEquals(List.of(existente), gravados);
    }

    @Test
    public void atendidoNaoPodeMaisSerEditado() throws Exception
    {
        Paciente atendido = paciente("Maria Souza", Gravidade.CRITICO, AGORA.minusDays(3));
        // Ainda não há fluxo que grave o atendimento (HU-08); o status é posto direto para testar a regra.
        java.lang.reflect.Field status = Paciente.class.getDeclaredField("status");
        status.setAccessible(true);
        status.set(atendido, StatusPaciente.ATENDIDO);
        naTabela.add(atendido);

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.editar(medico.getId(), UUID.randomUUID(), pedidoValido()));

        Assertions.assertEquals("O paciente Maria Souza está ATENDIDO e não pode mais ser editado: só paciente "
                + "AGUARDANDO pode.", erro.getMessage());
        Assertions.assertEquals("Maria Souza", atendido.getNome());
        Assertions.assertTrue(gravados.isEmpty());
    }

    @Test
    public void removeCadastroSemHistorico()
    {
        Paciente existente = paciente("Maria Souza", Gravidade.CRITICO, AGORA);
        naTabela.add(existente);

        servico.remover(medico.getId(), UUID.randomUUID());

        Assertions.assertEquals(List.of(existente), removidos);
    }

    @Test
    public void pacienteComHistoricoNaoERemovido()
    {
        naTabela.add(paciente("Maria Souza", Gravidade.CRITICO, AGORA));
        vinculos.put("campanhas", 1L);
        vinculos.put("procedimentos", 2L);

        RegraDeNegocioException erro = Assertions.assertThrows(RegraDeNegocioException.class,
                () -> servico.remover(medico.getId(), UUID.randomUUID()));

        Assertions.assertEquals("O paciente Maria Souza não pode ser removido: ele tem registros que precisam "
                + "ficar no histórico (1 campanha de doação e 2 procedimentos).", erro.getMessage());
        Assertions.assertTrue(removidos.isEmpty());
    }

    @Test
    public void removerTambemExigeMedico()
    {
        naTabela.add(paciente("Maria Souza", Gravidade.CRITICO, AGORA));

        Assertions.assertThrows(AcessoNegadoException.class, () -> servico.remover(admin.getId(), UUID.randomUUID()));
        Assertions.assertTrue(removidos.isEmpty());
    }
}
