package com.rotavital.dominio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Expediente do hemocentro para doação (HU-10, PI3-204): segunda a sábado, das 07h às 19h,
 * com horários de meia em meia hora e um limite de doadores por horário. É a mesma regra que
 * confere o agendamento e que monta a lista de horários do Portal do Doador.
 * Os horários chegam já no fuso do hemocentro (RelogioConfig.FUSO).
 */
public final class AgendaDoHemocentro
{
    public static final LocalTime ABRE = LocalTime.of(7, 0);
    // Último horário que aceita doador: quem chega às 19h ainda é atendido.
    public static final LocalTime ULTIMO_HORARIO = LocalTime.of(19, 0);
    public static final int MINUTOS_POR_HORARIO = 30;
    // Cadeiras de coleta: mais que isso no mesmo horário formaria fila na recepção.
    public static final int VAGAS_POR_HORARIO = 3;
    public static final String DESCRICAO = "Segunda a sábado, das 07h às 19h";

    private AgendaDoHemocentro()
    {
    }

    public static boolean abreNo(LocalDate dia)
    {
        return dia.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    // Os horários de um dia, de meia em meia hora; vazio no domingo.
    public static List<LocalTime> horariosDo(LocalDate dia)
    {
        List<LocalTime> horarios = new ArrayList<>();
        if (!abreNo(dia))
        {
            return horarios;
        }

        for (LocalTime hora = ABRE; !hora.isAfter(ULTIMO_HORARIO); hora = hora.plusMinutes(MINUTOS_POR_HORARIO))
        {
            horarios.add(hora);
        }
        return horarios;
    }

    public static boolean dentroDoExpediente(ZonedDateTime horario)
    {
        LocalTime hora = horario.toLocalTime();
        return abreNo(horario.toLocalDate()) && !hora.isBefore(ABRE) && !hora.isAfter(ULTIMO_HORARIO);
    }

    // 09:00 e 09:30 são horários da agenda; 09:10 não.
    public static boolean ehInicioDeHorario(ZonedDateTime horario)
    {
        return horario.getMinute() % MINUTOS_POR_HORARIO == 0 && horario.getSecond() == 0 && horario.getNano() == 0;
    }
}
