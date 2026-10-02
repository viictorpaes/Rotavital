import { useEffect, useState } from "react";
import {
  AlertTriangle,
  ArrowRight,
  CheckCircle2,
  Database,
  Layers,
  RefreshCw,
  Server,
  Terminal,
  XCircle,
} from "lucide-react";
import {
  obterDiagnostico,
  buscarEstoque,
  listarPontosRede,
  type DiagnosticoDados,
} from "@/services/api";

export default function PaginaAdmin()
{
  const [dados, setDados] = useState<DiagnosticoDados | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erroBackend, setErroBackend] = useState<string | null>(null);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [mensagemAcao, setMensagemAcao] = useState<string | null>(null);

  async function carregarDados()
  {
    try
    {
      setCarregando(true);
      const res = await obterDiagnostico();
      setDados(res);
      setErroBackend(null);
    }
    catch (err: any)
    {
      setErroBackend(err.message || "Não foi possível conectar com o back-end.");
      setDados(null);
    }
    finally
    {
      setCarregando(false);
    }
  }

  useEffect(() =>
  {
    carregarDados();

    if (!autoRefresh) return;
    const interval = setInterval(() =>
    {
      carregarDados();
    }, 3000);

    return () => clearInterval(interval);
  }, [autoRefresh]);

  async function dispararTesteEstoque()
  {
    try
    {
      setMensagemAcao("Testando requisição de estoque...");
      const res = await buscarEstoque("BS-01");
      setMensagemAcao(`✅ Sucesso! Estoque do BS-01 retornou ${res.totalBolsas} bolsa(s) do banco de dados.`);
      carregarDados();
    }
    catch (err: any)
    {
      setMensagemAcao(`❌ Erro no teste de estoque: ${err.message}`);
    }
  }

  async function dispararTestePontos()
  {
    try
    {
      setMensagemAcao("Testando requisição de pontos de rede...");
      const res = await listarPontosRede();
      setMensagemAcao(`✅ Sucesso! Rede retornou ${res.length} ponto(s) cadastrados no banco.`);
      carregarDados();
    }
    catch (err: any)
    {
      setMensagemAcao(`❌ Erro no teste de pontos: ${err.message}`);
    }
  }

  const backendOnline = dados !== null && dados.backend.status === "ONLINE";
  const bancoConectado = dados !== null && dados.bancoDeDados.status === "CONECTADO";
  const dadosPopulados = dados !== null && dados.bancoDeDados.estadoTabelas === "POPULADO";

  return (
    <div className="space-y-6">
      {/* Header */}
      <header className="flex flex-wrap items-center justify-between gap-4 border-b border-rota-border pb-5">
        <div>
          <div className="flex items-center gap-2">
            <span className="rounded bg-gray-900 px-2 py-0.5 font-mono text-[10px] font-bold uppercase tracking-wider text-white">
              Painel Administrativo
            </span>
            <span className="font-mono text-xs text-gray-500">Validação Full-Stack</span>
          </div>
          <h1 className="mt-1 text-2xl font-extrabold text-gray-900">
            Diagnóstico & Logs do Sistema
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            Monitoramento em tempo real da conexão entre Front-end, Back-end e Banco Supabase.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-rota-border bg-white px-3 py-1.5 text-xs font-medium text-gray-600 shadow-sm">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
              className="rounded text-rota-red focus:ring-rota-red"
            />
            Auto-atualizar (3s)
          </label>

          <button
            onClick={() => carregarDados()}
            disabled={carregando}
            className="flex items-center gap-2 rounded-lg bg-gray-900 px-4 py-2 text-xs font-semibold text-white shadow hover:bg-gray-800 disabled:opacity-50"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${carregando ? "animate-spin" : ""}`} />
            Atualizar Agora
          </button>
        </div>
      </header>

      {/* Alerta de Ação */}
      {mensagemAcao && (
        <div className="flex items-center justify-between rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-xs font-medium text-blue-900">
          <span>{mensagemAcao}</span>
          <button
            onClick={() => setMensagemAcao(null)}
            className="text-xs text-blue-600 underline hover:text-blue-800"
          >
            Fechar
          </button>
        </div>
      )}

      {/* Alerta de Erro de Conexão com Back-end */}
      {erroBackend && (
        <div className="flex items-center justify-between rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-xs font-medium text-rose-900">
          <div className="flex items-center gap-2">
            <XCircle className="h-4 w-4 shrink-0 text-rose-600" />
            <span>
              <strong>Falha de conexão com o Back-end:</strong> {erroBackend}. Verifique se o servidor Spring Boot está em execução na porta 8080.
            </span>
          </div>
          <button
            onClick={() => carregarDados()}
            className="text-xs font-semibold text-rose-700 underline hover:text-rose-900"
          >
            Tentar Novamente
          </button>
        </div>
      )}

      {/* Grid de Validação dos 3 Pilares */}
      <div className="grid grid-cols-1 gap-5 md:grid-cols-3">
        {/* Card 1: Front-end -> Back-end */}
        <div className="rounded-2xl border border-rota-border bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
              <Server className="h-5 w-5" />
            </span>
            {backendOnline ? (
              <span className="flex items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700">
                <CheckCircle2 className="h-3.5 w-3.5" /> Back-end Online
              </span>
            ) : (
              <span className="flex items-center gap-1.5 rounded-full bg-rose-50 px-2.5 py-1 text-xs font-bold text-rose-700">
                <XCircle className="h-3.5 w-3.5" /> Back-end Offline
              </span>
            )}
          </div>

          <h3 className="mt-4 font-semibold text-gray-900">1. Front-end ↔ Back-end</h3>
          <p className="mt-1 text-xs text-gray-500">Comunicação REST via Vite Proxy e CORS</p>

          <div className="mt-4 space-y-2 border-t border-gray-100 pt-3 font-mono text-xs">
            <div className="flex justify-between text-gray-600">
              <span>Porta:</span>
              <span className="font-semibold text-gray-900">8080 (Spring Boot)</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Java Runtime:</span>
              <span className="font-semibold text-gray-900">{dados?.backend.versaoJava || "—"}</span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Uptime:</span>
              <span className="font-semibold text-gray-900">
                {dados?.backend.uptimeSegundos ? `${dados.backend.uptimeSegundos}s` : "—"}
              </span>
            </div>
          </div>
        </div>

        {/* Card 2: Back-end -> Supabase */}
        <div className="rounded-2xl border border-rota-border bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-purple-50 text-purple-600">
              <Database className="h-5 w-5" />
            </span>
            {bancoConectado ? (
              <span className="flex items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700">
                <CheckCircle2 className="h-3.5 w-3.5" /> Banco Conectado
              </span>
            ) : (
              <span className="flex items-center gap-1.5 rounded-full bg-rose-50 px-2.5 py-1 text-xs font-bold text-rose-700">
                <XCircle className="h-3.5 w-3.5" /> Falha no Banco
              </span>
            )}
          </div>

          <h3 className="mt-4 font-semibold text-gray-900">2. Back-end ↔ Supabase</h3>
          <p className="mt-1 text-xs text-gray-500">Conexão JDBC PostgreSQL Session Pooler</p>

          <div className="mt-4 space-y-2 border-t border-gray-100 pt-3 font-mono text-xs">
            <div className="flex justify-between text-gray-600">
              <span>Latência:</span>
              <span className="font-semibold text-emerald-600">
                {dados?.bancoDeDados.latenciaMs ? `${dados.bancoDeDados.latenciaMs} ms` : "—"}
              </span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Catálogo / Schema:</span>
              <span className="font-semibold text-gray-900">
                {dados?.bancoDeDados.catalogo} / {dados?.bancoDeDados.schema}
              </span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Status Pool:</span>
              <span className="font-semibold text-gray-900">HikariCP Ativo</span>
            </div>
          </div>
        </div>

        {/* Card 3: Registros e Tabelas */}
        <div className="rounded-2xl border border-rota-border bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 text-amber-600">
              <Layers className="h-5 w-5" />
            </span>
            {dadosPopulados ? (
              <span className="flex items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700">
                <CheckCircle2 className="h-3.5 w-3.5" /> Tabelas Populadas
              </span>
            ) : (
              <span className="flex items-center gap-1.5 rounded-full bg-amber-50 px-2.5 py-1 text-xs font-bold text-amber-700">
                <AlertTriangle className="h-3.5 w-3.5" /> Rodar seed.sql
              </span>
            )}
          </div>

          <h3 className="mt-4 font-semibold text-gray-900">3. Dados no Banco</h3>
          <p className="mt-1 text-xs text-gray-500">Linhas gravadas nas tabelas do Supabase</p>

          <div className="mt-4 space-y-2 border-t border-gray-100 pt-3 font-mono text-xs">
            <div className="flex justify-between text-gray-600">
              <span>Bolsas em Estoque:</span>
              <span className="font-bold text-rota-red">
                {dados?.bancoDeDados.totalBolsasEstoque ?? 0}
              </span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Pontos da Rede:</span>
              <span className="font-semibold text-gray-900">
                {dados?.bancoDeDados.totalPontosRede ?? 0}
              </span>
            </div>
            <div className="flex justify-between text-gray-600">
              <span>Conexões / Rotas:</span>
              <span className="font-semibold text-gray-900">
                {dados?.bancoDeDados.totalConexoes ?? 0}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Botões de Ação Rápida */}
      <section className="rounded-2xl border border-rota-border bg-white p-5 shadow-sm">
        <h3 className="font-semibold text-gray-900">Simulador de Requisições</h3>
        <p className="text-xs text-gray-500">
          Dispare chamadas de API reais agora mesmo para vê-las no console de logs abaixo:
        </p>

        <div className="mt-4 flex flex-wrap gap-3">
          <button
            onClick={dispararTesteEstoque}
            className="flex items-center gap-2 rounded-lg border border-rota-border bg-gray-50 px-3.5 py-2 text-xs font-semibold text-gray-800 transition hover:bg-gray-100"
          >
            <span>🩸 Testar Consulta de Estoque</span>
            <ArrowRight className="h-3.5 w-3.5 text-gray-400" />
          </button>

          <button
            onClick={dispararTestePontos}
            className="flex items-center gap-2 rounded-lg border border-rota-border bg-gray-50 px-3.5 py-2 text-xs font-semibold text-gray-800 transition hover:bg-gray-100"
          >
            <span>🏥 Testar Consulta de Rede/Pontos</span>
            <ArrowRight className="h-3.5 w-3.5 text-gray-400" />
          </button>
        </div>
      </section>

      {/* Console de Logs em Tempo Real (Estilo Terminal) */}
      <section className="overflow-hidden rounded-2xl border border-gray-800 bg-gray-950 text-gray-200 shadow-xl">
        <div className="flex items-center justify-between border-b border-gray-800 px-5 py-3 bg-gray-900/60">
          <div className="flex items-center gap-2">
            <Terminal className="h-4 w-4 text-emerald-400" />
            <span className="font-mono text-xs font-semibold text-gray-300">
              Console de Requisições HTTP em Tempo Real
            </span>
          </div>
          <span className="font-mono text-[11px] text-gray-400">
            {dados?.logsRecentes.length || 0} requisições registradas
          </span>
        </div>

        <div className="max-h-[380px] overflow-y-auto p-4 font-mono text-xs space-y-2">
          {(!dados?.logsRecentes || dados.logsRecentes.length === 0) ? (
            <p className="py-6 text-center text-gray-500">
              Nenhuma requisição HTTP registrada ainda. Navegue pelas telas ou clique nos botões de teste acima.
            </p>
          ) : (
            dados.logsRecentes.map((log) =>
            {
              const isOk = log.status >= 200 && log.status < 300;
              const isWarn = log.status >= 400 && log.status < 500;

              return (
                <div
                  key={log.id}
                  className="flex flex-wrap items-center justify-between gap-2 rounded border border-gray-800/80 bg-gray-900/40 px-3 py-2 transition hover:bg-gray-900/80"
                >
                  <div className="flex items-center gap-3">
                    <span className="text-gray-500">{log.horario}</span>
                    <span
                      className={`rounded px-1.5 py-0.5 text-[10px] font-bold ${
                        isOk
                          ? "bg-emerald-950 text-emerald-400 border border-emerald-800"
                          : isWarn
                          ? "bg-amber-950 text-amber-400 border border-amber-800"
                          : "bg-rose-950 text-rose-400 border border-rose-800"
                      }`}
                    >
                      {log.status} {log.statusTag}
                    </span>
                    <span className="font-bold text-gray-300">{log.metodo}</span>
                    <span className="text-gray-100">{log.rota}</span>
                  </div>

                  <span className="text-emerald-400/80">{log.duracaoMs} ms</span>
                </div>
              );
            })
          )}
        </div>
      </section>
    </div>
  );
}
