package com.rotavital.servico;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rotavital.dominio.CampanhaDoacao;
import com.rotavital.dominio.Coordenada;
import com.rotavital.dominio.Endereco;
import com.rotavital.dominio.FonteDoTrajeto;
import com.rotavital.dominio.Gravidade;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.Paciente;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.Sexo;
import com.rotavital.dominio.TipoComponente;
import com.rotavital.dominio.TipoSanguineo;
import com.rotavital.dominio.Trajeto;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.CampanhaDoacaoRepository;
import com.rotavital.repositorio.UsuarioRepository;

// Rota do doador até o hospital do paciente da campanha (PI3-262), com o Google e o banco falsos.
public class RotaAteHospitalServiceTest
{
    private static final OffsetDateTime AGORA = OffsetDateTime.of(2026, 11, 3, 10, 0, 0, 0, ZoneOffset.ofHours(-3));
    private static final UUID ID_CAMPANHA = UUID.fromString("7d0c4b8e-0000-4000-8000-000000000237");

    // Doador no Hemope Central, indo ao Hospital das Clínicas (coordenadas do seed).
    private static final double LATITUDE_DOADOR = -8.0578;
    private static final double LONGITUDE_DOADOR = -34.8829;

    private final Usuario doador = new Usuario(UUID.randomUUID(), null, "João Doador", PapelUsuario.DOADOR, null,
            TipoSanguineo.O_NEGATIVO);
    private final Hospital hospital = new Hospital("hc-pe", "Hospital das Clínicas de PE",
            new Endereco("Av. Prof. Moraes Rego, 1235 - Cidade Universitária", -8.0512, -34.9478));

    // O que o "Google" recebeu e o que ele vai responder.
    private final List<Coordenada[]> pedidosAoGoogle = new ArrayList<>();
    private Optional<Trajeto> respostaDoGoogle = Optional.empty();
    private RotaAteHospitalService servico;

    @BeforeEach
    public void montarServico()
    {
        Paciente paciente = new Paciente(hospital, "Maria Souza", Sexo.FEMININO, LocalDate.of(1990, 5, 10),
                TipoSanguineo.O_NEGATIVO, TipoComponente.HEMACIAS, 2, "Cirurgia cardíaca", Gravidade.CRITICO, AGORA);
        CampanhaDoacao campanha = new CampanhaDoacao(paciente, null, AGORA);

        CampanhaDoacaoRepository campanhas = RepositorioFalso.respondendo(CampanhaDoacaoRepository.class, "findById",
                argumentos -> Optional.ofNullable(ID_CAMPANHA.equals(argumentos[0]) ? campanha : null));
        UsuarioRepository usuarios = RepositorioFalso.respondendo(UsuarioRepository.class, "findById",
                argumentos -> Optional.ofNullable(doador.getId().equals(argumentos[0]) ? doador : null));
        CalculadoraDeTrajeto google = (origem, destino) ->
        {
            pedidosAoGoogle.add(new Coordenada[] { origem, destino });
            return respostaDoGoogle;
        };

        servico = new RotaAteHospitalService(campanhas, google, new IdentificacaoService(usuarios));
    }

    @Test
    public void semLocalizacaoDevolveSoODestinoENemChamaOGoogle()
    {
        RotaAteHospital rota = servico.calcular(doador.getId(), ID_CAMPANHA, null, null);

        Assertions.assertEquals(hospital, rota.hospital());
        Assertions.assertNull(rota.trajeto());
        Assertions.assertTrue(pedidosAoGoogle.isEmpty());
    }

    @Test
    public void destinoSaoAsCoordenadasCadastradasDoHospitalDoPaciente()
    {
        respostaDoGoogle = Optional.of(new Trajeto(9.8, 22, FonteDoTrajeto.GOOGLE_ROUTES));

        servico.calcular(doador.getId(), ID_CAMPANHA, LATITUDE_DOADOR, LONGITUDE_DOADOR);

        Coordenada[] pedido = pedidosAoGoogle.get(0);
        Assertions.assertEquals(new Coordenada(LATITUDE_DOADOR, LONGITUDE_DOADOR), pedido[0]);
        Assertions.assertEquals(new Coordenada(-8.0512, -34.9478), pedido[1]);
    }

    @Test
    public void comLocalizacaoDevolveODistanciaETempoDoGoogle()
    {
        respostaDoGoogle = Optional.of(new Trajeto(9.8, 22, FonteDoTrajeto.GOOGLE_ROUTES));

        RotaAteHospital rota = servico.calcular(doador.getId(), ID_CAMPANHA, LATITUDE_DOADOR, LONGITUDE_DOADOR);

        Assertions.assertEquals(new Trajeto(9.8, 22, FonteDoTrajeto.GOOGLE_ROUTES), rota.trajeto());
        Assertions.assertEquals(ID_CAMPANHA, rota.campanhaId());
    }

    @Test
    public void googleSemRespostaViraEstimativaEmLinhaReta()
    {
        RotaAteHospital rota = servico.calcular(doador.getId(), ID_CAMPANHA, LATITUDE_DOADOR, LONGITUDE_DOADOR);

        Assertions.assertEquals(FonteDoTrajeto.ESTIMATIVA, rota.trajeto().fonte());
        Assertions.assertEquals(9.34, rota.trajeto().distanciaKm(), 0.01);
        Assertions.assertEquals(23, rota.trajeto().duracaoMinutos());
    }

    @Test
    public void campanhaQueNaoExisteDa404()
    {
        UUID outra = UUID.randomUUID();

        RecursoNaoEncontradoException erro = Assertions.assertThrows(RecursoNaoEncontradoException.class,
                () -> servico.calcular(doador.getId(), outra, LATITUDE_DOADOR, LONGITUDE_DOADOR));

        Assertions.assertEquals("Nenhuma campanha encontrada com id " + outra + ".", erro.getMessage());
    }

    @Test
    public void latitudeSemLongitudeDa400()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.calcular(doador.getId(), ID_CAMPANHA, LATITUDE_DOADOR, null));

        Assertions.assertEquals("Localização inválida: informe latitude e longitude juntas.", erro.getMessage());
        Assertions.assertTrue(pedidosAoGoogle.isEmpty());
    }

    @Test
    public void coordenadasForaDoMapaDao400ComOsDoisProblemas()
    {
        DadosInvalidosException erro = Assertions.assertThrows(DadosInvalidosException.class,
                () -> servico.calcular(doador.getId(), ID_CAMPANHA, 95.0, -200.0));

        Assertions.assertEquals("Localização inválida: latitude precisa estar entre -90 e 90; "
                + "longitude precisa estar entre -180 e 180.", erro.getMessage());
    }

    @Test
    public void semLoginDa401()
    {
        UsuarioNaoIdentificadoException erro = Assertions.assertThrows(UsuarioNaoIdentificadoException.class,
                () -> servico.calcular(null, ID_CAMPANHA, LATITUDE_DOADOR, LONGITUDE_DOADOR));

        Assertions.assertEquals("Entre no sistema para calcular a rota até o hospital.", erro.getMessage());
        Assertions.assertTrue(pedidosAoGoogle.isEmpty());
    }
}
