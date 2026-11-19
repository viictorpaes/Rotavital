package com.rotavital.servico;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rotavital.dominio.Coordenada;
import com.rotavital.dominio.Hospital;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.Trajeto;
import com.rotavital.repositorio.CampanhaDoacaoRepository;

/**
 * PI3-262 — Rota do ponto atual do doador até o hospital onde está o paciente da campanha.
 *
 * O destino sai do banco (as coordenadas cadastradas do hospital do paciente), nunca da tela:
 * assim o doador é levado exatamente ao hospital certo. A distância e o tempo vêm do Google
 * (Routes API, com o trânsito do momento); se ele não responder, vem a estimativa em linha reta.
 *
 * Sem @Transactional de propósito: a campanha é lida numa consulta curta (o findById já é
 * transacional e traz paciente e hospital juntos) e a chamada ao Google, que pode levar alguns
 * segundos, acontece sem prender uma conexão do banco.
 */
@Service
public class RotaAteHospitalService
{
    private final CampanhaDoacaoRepository campanhas;
    private final CalculadoraDeTrajeto calculadora;
    private final IdentificacaoService identificacao;

    public RotaAteHospitalService(CampanhaDoacaoRepository campanhas, CalculadoraDeTrajeto calculadora,
            IdentificacaoService identificacao)
    {
        this.campanhas = campanhas;
        this.calculadora = calculadora;
        this.identificacao = identificacao;
    }

    /**
     * Sem latitude e longitude (o doador negou a localização), devolve só o destino. Exige alguém
     * logado: cada cálculo consome a cota da Routes API, então a rota não fica aberta a qualquer um.
     */
    public RotaAteHospital calcular(UUID usuarioId, UUID campanhaId, Double latitude, Double longitude)
    {
        identificacao.exigir(usuarioId, "calcular a rota até o hospital",
                PapelUsuario.DOADOR, PapelUsuario.MEDICO, PapelUsuario.ADMIN);

        Hospital hospital = campanhas.findById(campanhaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhuma campanha encontrada com id " + campanhaId + "."))
                .getPaciente()
                .getHospital();

        if (latitude == null && longitude == null)
        {
            return new RotaAteHospital(campanhaId, hospital, null);
        }

        Coordenada origem = origemValidada(latitude, longitude);
        Coordenada destino = new Coordenada(hospital.getLatitude(), hospital.getLongitude());
        Trajeto trajeto = calculadora.calcular(origem, destino)
                .orElseGet(() -> Trajeto.estimadoEntre(origem, destino));

        return new RotaAteHospital(campanhaId, hospital, trajeto);
    }

    // Junta os problemas numa mensagem só, como o cadastro de pacientes faz.
    private static Coordenada origemValidada(Double latitude, Double longitude)
    {
        List<String> problemas = new ArrayList<>();

        if (latitude == null || longitude == null)
        {
            problemas.add("informe latitude e longitude juntas");
        }
        if (latitude != null && (!Double.isFinite(latitude) || latitude < -90 || latitude > 90))
        {
            problemas.add("latitude precisa estar entre -90 e 90");
        }
        if (longitude != null && (!Double.isFinite(longitude) || longitude < -180 || longitude > 180))
        {
            problemas.add("longitude precisa estar entre -180 e 180");
        }

        if (!problemas.isEmpty())
        {
            throw new DadosInvalidosException("Localização inválida: " + String.join("; ", problemas) + ".");
        }
        return new Coordenada(latitude, longitude);
    }
}
