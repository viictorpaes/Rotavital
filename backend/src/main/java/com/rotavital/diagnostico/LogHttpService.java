package com.rotavital.diagnostico;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Service;

@Service
public class LogHttpService
{
    private static final int MAX_LOGS = 60;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ConcurrentLinkedDeque<LogHttpItem> buffer = new ConcurrentLinkedDeque<>();

    public void registrar(String metodo, String rota, int status, long duracaoMs, String statusTag)
    {
        String horario = LocalTime.now().format(FORMATO_HORA);
        String id = UUID.randomUUID().toString().substring(0, 8);

        buffer.addFirst(new LogHttpItem(id, horario, metodo, rota, status, duracaoMs, statusTag));

        // Mantém o tamanho máximo fixo
        while (buffer.size() > MAX_LOGS)
        {
            buffer.pollLast();
        }
    }

    public List<LogHttpItem> getLogsRecentes()
    {
        return new ArrayList<>(buffer);
    }
}
