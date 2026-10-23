<h1 align="center">
  Rota Vital — Documentação Técnica <br> Figma × Sistema Real <br>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg"
       width="32"
       style="vertical-align: middle;">🩸🖼️
</h1>

<p align="center">
<a href="../README.md">⬅️ Voltar ao README principal</a> ·
<a href="DOCUMENTACAO.md">📚 Índice da documentação</a> ·
<a href="ENTREGAS_PI.md">📌 Histórias de usuário</a>
</p>

> Cada tela do protótipo do Figma ao lado da mesma tela no sistema rodando, com o trecho de código que a implementa.

<p align="center">
Comparação lado a lado entre o protótipo do Figma e a mesma tela no sistema rodando de verdade
(frontend React em <code>localhost:5173</code> + backend Spring Boot ligado ao Supabase), cobrindo
HU‑01, HU‑02, HU‑03, HU‑04/05/06, HU‑07, HU‑08, HU‑09, HU‑10 e a tela de Admin.
Prints tirados com Playwright (Chromium headless, janela 1440×900): Painel, Estoque, Portal do Doador e
Admin vêm das <a href="evidencias/PI3-182_perfis_de_acesso.md">evidências do PI3-182</a>
(10/10/2026), e as demais telas foram capturadas do mesmo jeito em 11/10/2026.
</p>

<p align="center">
<sub>O Estoque e os números de estoque do Painel já vêm do Supabase; as demais telas ainda usam os dados de
exemplo do frontend (ver <a href="ESTRUTURA_DO_PROJETO.md#notas">o que cada camada faz hoje</a>).</sub>
</p>

<h2 align="left">🔐 HU‑01 — Login (e-mail e senha)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_Login_Medico.png" width="420"/> | <img src="evidencias/PI3-184/01_login_senha_errada.png" width="320"/> |

<sub>O Figma escolhe o perfil por cards; o sistema real pede e-mail e senha e descobre o perfil pela API
(PI3-183/184). O print mostra o aviso de senha errada (PI3-196); os cenários estão nas
<a href="evidencias/PI3-184_PI3-196_login.md">evidências da PI3-184/196</a>.</sub>

<details>
<summary>🩸 <a href="../frontend/src/pages/Login/PaginaLogin.tsx"><code>frontend/src/pages/Login/PaginaLogin.tsx</code></a></summary>

```tsx
async function handleEntrar(evento: FormEvent<HTMLFormElement>)
{
  evento.preventDefault();
  if (!email.trim() || !senha)
  {
    setAviso(AVISO_CAMPOS_VAZIOS);
    return;
  }

  setEntrando(true);
  try
  {
    const acesso = await autenticarUsuario(email.trim(), senha); // POST /api/v1/acessos
    const logado = login(acesso);
    navigate(logado.telaInicial, { replace: true });             // /painel, /portal-doador ou /admin
  }
  catch (erro)
  {
    setAviso(avisoDoErro(erro)); // 401: "E-mail ou senha incorretos." — o mesmo para senha errada e e-mail sem cadastro
    setSenha("");
    setEntrando(false);
  }
}
```
</details>

<h2 align="left">📊 HU‑02 — Painel Operacional (início)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_PainelOperacional.png" width="420"/> | <img src="evidencias/PI3-182/02_medico_entra.png" width="420"/> |

<details>
<summary>🩸 <a href="../frontend/src/pages/PainelOperacional/PaginaPainel.tsx"><code>frontend/src/pages/PainelOperacional/PaginaPainel.tsx</code></a></summary>

```tsx
/**
 * HU-02 — Painel operacional: números rápidos, caixa de avisos ordenável e
 * atalhos, para o médico agir sem percorrer todas as telas. Indicadores e
 * avisos derivam dos mocks de estoque e de pacientes (ver `lib/painel.ts`).
 */
export default function PaginaPainel()
{
  const { lotes, pacientes } = useDados();
  const [ordenacao, setOrdenacao] = useState<OrdenacaoAvisos>("severidade");

  const avisos = useMemo(() => montarAvisos(lotes, pacientes), [lotes, pacientes]);
  const indicadores = useMemo(() => montarIndicadores(lotes, avisos), [lotes, avisos]);
  const avisosOrdenados = useMemo(() => ordenarAvisos(avisos, ordenacao), [avisos, ordenacao]);
  // ...
}
```
</details>

<h2 align="left">📦 HU‑03 — Estoque (por tipo sanguíneo)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_Estoque.png" width="420"/> | <img src="evidencias/PI3-182/09_admin_abre_estoque.png" width="420"/> |

<sub>Print aberto pelo perfil admin, por isso o item "Diagnóstico & Logs" aparece no menu. As 8 bolsas são as do banco <code>BS-01</code> no Supabase.</sub>

<details>
<summary>🩸 <a href="../frontend/src/pages/Estoque/PaginaEstoque.tsx"><code>frontend/src/pages/Estoque/PaginaEstoque.tsx</code></a></summary>

```tsx
export default function PaginaEstoque()
{
  const [filtroTipo, setFiltroTipo] = useState<TipoSanguineo | "todos">("todos");
  const [filtroComponente, setFiltroComponente] = useState<TipoComponenteBackend | "todos">("todos");
  const [filtroStatus, setFiltroStatus] = useState<StatusBolsaBackend | "todos">("todos");
  const [pagina, setPagina] = useState(0);
  // ...

  useEffect(() =>
  {
    listarHemocomponentes({                       // GET /api/v1/hemocomponentes
      bancoId: BANCO_ID,
      tipoSanguineo: filtroTipo === "todos" ? undefined : paraTipoSanguineoBackend(filtroTipo),
      tipoComponente: filtroComponente === "todos" ? undefined : filtroComponente,
      status: filtroStatus === "todos" ? undefined : filtroStatus,
      page: pagina,
      size: TAMANHO_PAGINA,
    })
      .then((bolsas) => setLotes(bolsas.map(converterBolsaParaLote)))
      .catch((falha) => setErro(falha.message));  // mostra o erro; não troca por dados de exemplo
  }, [filtroTipo, filtroComponente, filtroStatus, pagina, recarga]);
  // ...
}
```
</details>

<details>
<summary>➕ <a href="../frontend/src/components/estoque/ModalNovaBolsa.tsx"><code>frontend/src/components/estoque/ModalNovaBolsa.tsx</code></a> — cadastro de bolsa (PI3-179)</summary>

```tsx
// noValidate no <form>: quem valida é a API, e o formulário mostra a mensagem que ela devolveu.
async function aoEnviar(evento: FormEvent<HTMLFormElement>)
{
  evento.preventDefault();
  try
  {
    const bolsa = await cadastrarHemocomponente({   // POST /api/v1/hemocomponentes
      tipoComponente: componente,
      tipoSanguineo: paraTipoSanguineoBackend(tipoSanguineo),
      dataColeta: dataColeta || null,              // em branco vai como null: a API diz qual campo falta
      dataValidade: dataValidade || null,
      loteSintetico: lote.trim(),
      volumeMl: paraNumero(volume),
      temperaturaCelsius: paraNumero(temperatura),
      localizacao: localizacao.trim() || undefined,
      bancoOrigemId: bancoId,
    });
    onCadastrada(bolsa);                           // a página fecha o modal, avisa e recarrega a lista
  }
  catch (falha: unknown)
  {
    setErro(falha instanceof ErroHttp ? falha.detalhe || falha.message : "Não foi possível conectar com o back-end. A bolsa não foi registrada.");
  }
}
```

Trocar o componente refaz as sugestões: a validade vira a coleta + o prazo máximo (hemácias 42 dias, plasma 730,
plaquetas 7, crioprecipitado 365) e a temperatura, o meio da faixa ideal. Depois do cadastro, a lista passa a filtrar
pelo tipo e componente da bolsa nova, da primeira página, para ela aparecer na tela.
</details>

<h2 align="left">📋 HU‑04/05/06 — Requisição de hemocomponente (solicitar)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_Requisicao_Solicitar.png" width="420"/> | <img src="../img/real-requisicao_solicitar.png" width="420"/> |

<h2 align="left">📋 HU‑04/05/06 — Requisição de hemocomponente (recebidas)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_Requisicao_Recebidas.png" width="420"/> | <img src="../img/real-requisicao_recebidas.png" width="420"/> |

<h2 align="left">📋 HU‑04/05/06 — Requisição de hemocomponente (campanha)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_Requisicao_Campanha.png" width="420"/> | <img src="../img/real-requisicao_campanha.png" width="420"/> |

<details>
<summary>🩸 <a href="../frontend/src/pages/Requisicao/PaginaRequisicao.tsx"><code>frontend/src/pages/Requisicao/PaginaRequisicao.tsx</code></a></summary>

```tsx
/**
 * HU-04, HU-05 e HU-06 — Solicitação clínica em três abas: solicitar um
 * hemocomponente (com sugestão FEFO), conferir remessas recebidas e publicar
 * uma campanha de doação. Dados ainda mockados (ver `context/ContextoDados.tsx`).
 */
export default function PaginaRequisicao()
{
  const { lotes, recebimentosPendentes } = useDados();
  const [aba, setAba] = useState<Aba>("solicitar");

  const vencendo = useMemo
  (
    () => lotes.filter((lote) => diasAteVencer(lote) <= JANELA_VENCIMENTO_DIAS).length,
    [lotes],
  );
  // ...
}
```
</details>

<h2 align="left">🗺️ HU‑07 — Rede hospitalar (mapa e rota de transporte)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_RedeHospitalar.png" width="420"/> | <img src="../img/real-rede_hospitalar.png" width="420"/> |

<details>
<summary>🩸 <a href="../frontend/src/pages/RedeHospitalar/PaginaRede.tsx"><code>frontend/src/pages/RedeHospitalar/PaginaRede.tsx</code></a></summary>

```tsx
/**
 * HU-07 — Mapa dos hospitais conectados e plano de transporte na cadeia fria.
 * Selecionar um hospital (no mapa ou na lista) recalcula a rota e o plano.
 * Dados mockados nesta primeira versão (ver `data/redeMock.ts`).
 */
export default function PaginaRede()
{
  const [selecionado, setSelecionado] = useState<PontoDeRede>(hospitais[0]);
  const [componente, setComponente] = useState<TipoComponente>("Concentrado de Hemácias");
  const { rotas, carregando } = useRotasDaRede();
  const rota = rotas[selecionado.id];
  // ...
}
```
</details>

<h2 align="left">🏥 HU‑08 — Pacientes (fila clínica)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_Pacientes.png" width="420"/> | <img src="../img/real-pacientes.png" width="420"/> |

<h2 align="left">🏥 HU‑08 — Pacientes (concluir procedimento)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_Pacientes_ConcluirProcedimento.png" width="420"/> | <img src="../img/real-pacientes_concluirprocedimento.png" width="420"/> |

<details>
<summary>🩸 <a href="../frontend/src/pages/Pacientes/PaginaPacientes.tsx"><code>frontend/src/pages/Pacientes/PaginaPacientes.tsx</code></a></summary>

```tsx
/**
 * HU-08 — Fila clínica de pacientes com necessidade ativa. A conclusão do
 * procedimento tira o paciente da fila e, quando atendido pelo estoque interno,
 * baixa as unidades por FEFO.
 */
function handleConfirmar(origem: OrigemAtendimento)
{
  if (!selecionado) 
  {
    return undefined;
  }

  return concluirProcedimento(selecionado.id, origem);
}
```
</details>

<h2 align="left">❤️ HU‑09 — Doações (lista de pessoas que precisam)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_Doacoes.png" width="420"/> | <img src="../img/real-doacoes.png" width="420"/> |

<details>
<summary>🩸 <a href="../frontend/src/pages/Doacoes/PaginaDoacoes.tsx"><code>frontend/src/pages/Doacoes/PaginaDoacoes.tsx</code></a></summary>

```tsx
const pessoasFiltradas = useMemo(() =>
{
  const termo = busca.trim().toLowerCase();

  return pessoasNecessitadas.filter((p) =>
  {
      const combinaTipo = filtroTipo === "todos" || p.tipoSanguineo === filtroTipo;
      const combinaBusca = 
      termo.length === 0 ||
      p.nome.toLowerCase().includes(termo) ||
      p.causa.toLowerCase().includes(termo) ||
      p.componente.toLowerCase().includes(termo);

    return combinaTipo && combinaBusca;
  });
}, [busca, filtroTipo]);
```

</details>

<h2 align="left">❤️ HU‑09 — Agende sua doação (modal)</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_Doacoes_AgendeSuaDoacao.png" width="420"/> | <img src="../img/real-doacoes_agendesuadoacao.png" width="420"/> |

<details>
<summary>🩸 <a href="../frontend/src/components/donations/ModalAgendarDoacao.tsx"><code>frontend/src/components/donations/ModalAgendarDoacao.tsx</code></a></summary>

```tsx
export function ModalAgendarDoacao({ pessoa, onClose }: Readonly<Props>)
{
  const [confirmado, setConfirmado] = useState(false);
  // ...
  <button
    type="button"
    onClick={() => setConfirmado(true)}
    disabled={confirmado}
    className="w-full rounded-lg bg-rota-red px-4 py-3 text-sm font-semibold text-white transition-colors hover:bg-rota-redDark disabled:cursor-default disabled:bg-emerald-600"
  >
    {confirmado ? "Rota traçada — até logo! 👋🏻" : "Confirmar e traçar rota no celular ✅"}
  </button>
}
```
</details>

<h2 align="left">👤 HU‑10 — Portal do Doador</h2>

| Figma | Sistema real |
| :---: | :---: |
| <img src="../img/FIGMA_PortalDoDoador.png" width="420"/> | <img src="evidencias/PI3-182/05_doador_entra.png" width="420"/> |

<details>
<summary>🩸 <a href="../frontend/src/pages/PortalDoador/PaginaPortalDoador.tsx"><code>frontend/src/pages/PortalDoador/PaginaPortalDoador.tsx</code></a></summary>

```tsx
export default function PaginaPortalDoador()
{
  const { usuario, logout } = useAutenticacao();
  // ...
  if (usuario?.papel !== "doador")
  {
    return <Navigate to="/login" replace />;
  }
  // ...
}
```
</details>

<h2 align="left">🛠️ Admin — Diagnóstico & Logs</h2>

| Figma | Sistema real |
| :---: | :---: |
| *(tela criada depois do protótipo)* | <img src="evidencias/PI3-182/08_admin_entra.png" width="420"/> |

<sub>Mostra a conexão frontend ↔ backend ↔ Supabase e as últimas requisições HTTP. Só o perfil admin abre esta tela (<a href="PERFIS_DE_ACESSO.md">PI3-182</a>).</sub>
