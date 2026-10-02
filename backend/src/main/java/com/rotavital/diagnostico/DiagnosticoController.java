package com.rotavital.diagnostico;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.sql.DataSource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/diagnostico")
@CrossOrigin(origins = "*")
public class DiagnosticoController
{
    private final DataSource dataSource;
    private final LogHttpService logHttpService;

    public DiagnosticoController(DataSource dataSource, LogHttpService logHttpService)
    {
        this.dataSource = dataSource;
        this.logHttpService = logHttpService;
    }

    @GetMapping
    public ResponseEntity<DiagnosticoResponse> obterDiagnostico()
    {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        DiagnosticoResponse.BackendInfo backendInfo = new DiagnosticoResponse.BackendInfo(
                "ONLINE",
                uptime,
                System.getProperty("java.version"),
                "3.3.4",
                dataHora
        );

        DiagnosticoResponse.BancoInfo bancoInfo;
        long inicioDb = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection())
        {
            long latencia = System.currentTimeMillis() - inicioDb;
            DatabaseMetaData meta = conn.getMetaData();
            String produto = meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
            String catalogo = conn.getCatalog();
            String schema = conn.getSchema();
            String url = meta.getURL();

            // Mascara credenciais na URL
            String urlMascarada = url != null ? url.replaceAll("password=[^&]*", "password=******") : "Desconhecida";

            long totalPontos = contarTabela(conn, "ponto_rede");
            long totalBolsas = contarTabela(conn, "bolsa_hemocomponente");
            long totalConexoes = contarTabela(conn, "conexao");

            String estadoTabelas = (totalPontos > 0 || totalBolsas > 0)
                    ? "POPULADO"
                    : "VAZIO_EXECUTE_SEED_SQL";

            bancoInfo = new DiagnosticoResponse.BancoInfo(
                    "CONECTADO",
                    latencia,
                    produto,
                    catalogo,
                    schema,
                    urlMascarada,
                    totalPontos,
                    totalBolsas,
                    totalConexoes,
                    estadoTabelas,
                    null
            );
        }
        catch (Exception e)
        {
            long latencia = System.currentTimeMillis() - inicioDb;
            bancoInfo = new DiagnosticoResponse.BancoInfo(
                    "ERRO_CONEXAO",
                    latencia,
                    "Desconhecido",
                    "-",
                    "-",
                    "-",
                    0,
                    0,
                    0,
                    "INDISPONIVEL",
                    e.getMessage()
            );
        }

        return ResponseEntity.ok(new DiagnosticoResponse(
                backendInfo,
                bancoInfo,
                logHttpService.getLogsRecentes()
        ));
    }

    private long contarTabela(Connection conn, String tabela)
    {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM public." + tabela))
        {
            if (rs.next())
            {
                return rs.getLong(1);
            }
        }
        catch (Exception ignored)
        {
            // Tabela pode ainda não ter sido criada
        }
        return 0;
    }
}
