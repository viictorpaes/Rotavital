<h1 align="center">Rota Vital — Roteiro-exemplo do MVP 🎬🩸 <br>
<img src="https://img.shields.io/badge/-Screencasts_·_Entregas_03_e_04-111827?style=flat-square&logo=youtube&logoColor=red"/>
<img src="https://img.shields.io/badge/-Apresentação_final-111827?style=flat-square&logo=googleslides&logoColor=FBBC04"/></h1>

> Roteiro para gravar os vídeos das Entregas 03 e 04 e a demonstração da apresentação final. As falas são exemplos:
> adapte ao seu jeito, mas mantenha a ordem das cenas, porque cada uma depende da anterior.
>
> **Ensaiado de ponta a ponta em 06/11/2026**, com banco zerado, pelo `compose.yaml` e sem as chaves do Google. Os valores
> citados são os que apareceram na tela nesse ensaio. Não foram ensaiados o envio de `/cadastro-doador` (cria conta real
> no Supabase Auth) e o sistema com as chaves do Google.

**Sumário:** [1. O que gravar](#1-o-que-gravar) · [2. Antes de gravar](#2-antes-de-gravar) ·
[3. Dados da história](#3-dados-da-historia) · [4. Cenas de uso](#4-cenas-de-uso) ·
[5. Explicação do código](#5-explicacao-do-codigo) · [6. Apresentação final](#6-apresentacao-final) ·
[7. O que não mostrar](#7-o-que-nao-mostrar) · [8. Se algo der errado](#8-se-algo-der-errado)

<h2 id="1-o-que-gravar">1. O que gravar</h2>

Os vídeos vão para o YouTube, com áudio ou legenda, e o link entra no README, na tabela de artefatos da entrega.

| Vídeo | Conteúdo | Tempo sugerido |
| :--- | :--- | :---: |
| Entrega 03 · uso do sistema | Cenas 1 a 3 | 3 min |
| Entrega 03 · explicação do código | Blocos A e B | 4 min |
| Entrega 04 · uso do sistema | Cenas 4 a 9 | 6 min |
| Entrega 04 · explicação do código | Blocos C, D e E | 6 min |
| Apresentação final (09 a 13/11) | Seção 6, com um corte das cenas | até 8 min |

<h2 id="2-antes-de-gravar">2. Antes de gravar</h2>

1. **Zere o banco local**, antes da primeira gravação e entre uma tentativa e outra:

   ```bash
   npm run docker:down:v   # apaga só o banco local
   npm run docker:local    # banco + API + site → http://localhost:8080
   ```

   Sem isso, a bolsa enviada não volta para o estoque, o doador continua com doação em aberto e as migrations novas
   não rodam num volume antigo. As validades do seed contam a partir da subida do banco: zerando no dia, a primeira
   bolsa aparece vencendo em 3 dias, como no roteiro.
2. **Não grave com `npm run docker:cloud`:** cada tentativa ficaria gravada no banco do time, no Supabase.
3. **Confira o `.env` e a internet.** O login passa pelo Supabase Auth mesmo com o banco local, então precisa da
   `SUPABASE_KEY`. O mapa e o traçado das rotas também vêm da internet.
4. **Prepare duas abas no mesmo navegador.** A sessão fica em cada aba, então médico e doador convivem:
   - **Aba 1 (médico):** deixe em `http://localhost:8080/login`, sem entrar.
   - **Aba 2 (doador):** entre com `doador@rotavital.test` e **aperte F5**. Sem o F5 aparece um aviso vermelho de perfil
     (ver a [seção 8](#8-se-algo-der-errado)).
5. **Libere a localização** para `localhost` no navegador. O pedido de permissão não aparece no meio da cena 6.
6. **Deixe a tela limpa:** zoom em 100%, notificações desligadas, sem outras abas à vista e com o microfone testado.
7. **Escolha o dia da doação:** o próximo dia de segunda a sábado. No dia de hoje, os horários que já passaram ficam
   bloqueados.

**Com ou sem as chaves do Google?** O sistema funciona sem elas. Decida antes, porque muda o que aparece na tela:

| | Sem chaves (como no ensaio) | Com `GOOGLE_MAPS_API_KEY` e `GOOGLE_MAPS_BROWSER_KEY` no `.env` |
| :--- | :--- | :--- |
| Mapa da Rede | OpenStreetMap | Google Maps |
| Como chegar | "Estimativa pela distância em linha reta · o Google Maps não respondeu" | Distância e tempo com trânsito |
| Melhor rota | 4,3 km · 12 min, das conexões cadastradas | Distância pelas ruas: os números mudam, o caminho do Dijkstra continua na tela |

Sem as chaves, diga na cena 6 que aquele é o plano B do sistema. Fica melhor do que deixar o aviso sem explicação.

<h2 id="3-dados-da-historia">3. Dados da história</h2>

A história tem um fio só: **uma paciente O- precisa de sangue no Hospital das Clínicas de PE.** A médica publica a campanha,
um doador agenda e, enquanto isso, o hemocentro envia uma bolsa pelo menor caminho.

Senhas dos usuários em [Usuários de teste](../README.md#usuarios-teste).

| Perfil | E-mail | Nome na tela |
| :--- | :--- | :--- |
| 🩺 Médico | `medico@rotavital.test` | Dra. Ana Lima |
| ❤️ Doador | `doador@rotavital.test` | Carlos Souza (O-) |

**Paciente fictícia da campanha**

| Campo | Valor |
| :--- | :--- |
| Nome do paciente | Ana Paula Ramos |
| Data de nascimento | 20/05/1988 |
| Hospital | Hospital das Clínicas de PE |
| Tipo sanguíneo | O- |
| Hemocomponente necessário | Concentrado de Hemácias |
| Causa / diagnóstico | Cirurgia cardíaca — precisa de reposição urgente |
| Nível de gravidade | Emergência |

**Bolsa nova da cena 3**

Tipo O-, Concentrado de Hemácias, coleta hoje, 450 mL, 4 °C, lote `LOTE-2026-0420`, localização `R1 · P4 · N1`. Na validade,
primeiro a data de **ontem** (para mostrar o erro) e depois **hoje + 35 dias**.

**Bolsas do seed que aparecem na história.** O Estoque mostra o **lote** e a Rede mostra o **id** da mesma bolsa:

| No Estoque | Na Rede | Bolsa | Papel na história |
| :--- | :--- | :--- | :--- |
| `LOTE-SIM-0001` | `CH-1042` | Hemácias O-, vence em 3 dias | Primeira da fila FEFO: é a que sai na cena 9 |
| `LOTE-SIM-0004` | `PQ-3014` | Plaquetas A+, a 25,6 °C | Fora da faixa (20 a 24 °C): é barrada na cena 8 |

<h2 id="4-cenas-de-uso">4. Cenas de uso</h2>

**Abertura de qualquer vídeo de uso (20 s), na tela de login:**

> "O Rota Vital é um sistema de logística de hemocomponentes. Ele controla o estoque de sangue com validade e temperatura,
> liga doadores a pacientes e calcula a rota das bolsas entre o hemocentro e os hospitais."

<h3>Entrega 03</h3>

**Cena 1 · Login por perfil (HU‑01) — 1 min**

| Na tela | Fala |
| :--- | :--- |
| Aba 1: `medico@rotavital.test` com senha errada → **Entrar** | "A entrada é por e-mail e senha, conferidos no Supabase Auth. Senha errada e e-mail que não existe dão o mesmo aviso, para a tela não revelar quem tem cadastro." |
| Senha certa → abre o Painel | "O perfil vem da API, e cada um cai na sua tela: o médico vê o painel, o doador vê o portal e o administrador, o diagnóstico." |
| Aba 2: abra `localhost:8080/estoque` → volta para o portal | "O doador não entra nas telas do hospital: quem tenta abrir o estoque volta para o portal." |

**Cena 2 · Painel e Estoque (HU‑03) — 1 min**

Valores no banco zerado: 6 unidades, 8 tipos em risco, 3 vencendo em 7 dias, 6 alertas críticos e 7 avisos.

| Na tela | Fala |
| :--- | :--- |
| Aba 1: Painel, Caixa de Avisos | "O painel resume o que pede ação: unidades, bolsas perto de vencer e alertas. Com o estoque de exemplo, todos os tipos estão abaixo do nível de segurança de 10 unidades." |
| Menu **Estoque** → filtro **A+** | "O estoque vem da API, com filtros por tipo, componente e status. Esta plaqueta está a 25,6 graus, acima da faixa ideal, e o sistema marca 'fora da faixa'." |
| Filtro **O-** | "Aqui, o lote em vermelho vence em 3 dias. A lista vem ordenada por validade: o que vence primeiro aparece primeiro." |

**Cena 3 · Nova bolsa (HU‑03) — 1 min**

| Na tela | Fala |
| :--- | :--- |
| **Nova bolsa** → preencha com a validade de **ontem** → **Registrar bolsa** | "Ao cadastrar, a API confere as regras. Validade antes da coleta não passa, e a mensagem diz exatamente o que corrigir." |
| Troque a validade para hoje + 35 dias → **Registrar bolsa** | "Com a data certa, a bolsa é gravada e já aparece no estoque, com o lote e a localização." |

Depois de gravar, aparece o aviso "Bolsa registrada · CH-1081".

<h3>Entrega 04</h3>

Se for gravar só esta parte, faça as cenas 1 e 3 antes, fora do vídeo. A cena 8 conta com a bolsa nova.

**Cena 4 · Campanha de doação (HU‑06) — 1 min**

| Na tela | Fala |
| :--- | :--- |
| Aba 1: **Requisição** → aba **Campanha** → preencha com os dados da paciente | "A paciente precisa de sangue O-. A médica publica uma campanha com o hospital, o hemocomponente e a gravidade." |
| **Publicar campanha de doação** → **Ver em Doações** | "Publicar grava o paciente e a campanha na mesma transação. Ela já aparece em Doações, em Pacientes e no portal do doador." |

**Cena 5 · Doador agenda (HU‑10, HU‑09) — 1 min 30**

| Na tela | Fala |
| :--- | :--- |
| Aba 2: **F5** → mostre o perfil e o cartão de Ana Paula Ramos | "No portal, o doador vê o próprio perfil e quem precisa, sem o menu do hospital." |
| **Fazer doação** → escolha um domingo | "A agenda respeita o hemocentro: no domingo ele não abre." |
| Escolha o dia certo → **09:00** → **Confirmar agendamento** → **Fechar** | "De segunda a sábado, das 7h às 19h, com até 3 doadores por horário. O sistema mostra as vagas e grava o agendamento." |
| Mostre **Minhas doações** | "Agora ele acompanha a doação e pode remarcar ou cancelar. Cada doador tem um agendamento em aberto por vez." |

**Cena 6 · Como chegar (PI3‑237) — 45 s**

| Na tela | Fala |
| :--- | :--- |
| **Como chegar** no cartão da paciente | "A localização só é usada nesta consulta, com o consentimento do doador, e não fica gravada. O destino são as coordenadas cadastradas do hospital." |
| Mostre distância e tempo (sem chaves) | "Aqui o sistema está no plano B: sem a resposta do Google, ele estima pela linha reta. Com a chave configurada, usa a rota com trânsito." |
| Mostre **Abrir no Google Maps** e **Abrir no Waze** → **Fechar** | "Os botões abrem a navegação no aplicativo do celular ou no site, no computador." |

A distância depende de onde você está gravando. No ensaio, com posição simulada, foram 10,9 km e 27 min.

**Cena 7 · Médico acompanha (HU‑09, HU‑08) — 45 s**

| Na tela | Fala |
| :--- | :--- |
| Aba 1: **Doações** → **F5** → **Agendamentos (1)** | "O médico vê quem agendou para cada campanha." |
| **Doação feita** → **Fechar** | "Ele registra se a doação foi feita, se o doador faltou ou se cancelou. Isso aparece no perfil do doador." |
| Menu **Pacientes** | "A paciente também entrou na fila clínica. Aqui o médico cadastra, edita e remove pacientes." |

Não clique em **Concluir procedimento** (ver a [seção 7](#7-o-que-nao-mostrar)).

**Cena 8 · Rede e Dijkstra (HU‑07) — 1 min**

| Na tela | Fala |
| :--- | :--- |
| Menu **Início** → **F5** | "Antes de enviar, o painel mostra 7 unidades, já com a bolsa nova, e 3 vencendo em 7 dias." |
| Menu **Rede** → **Hospital das Clínicas de PE** na lista | "O mapa mostra a rede real. A ligação direta tem 7,8 km, mas o Dijkstra achou 4,3 km passando pelo Barão de Lucena, o ponto laranja no mapa." |
| **Hemocomponente a enviar**: Concentrado de Plaquetas | "Cada componente tem sua faixa de transporte. A plaqueta do estoque está fora da faixa, então o sistema não deixa sair: 'Nenhuma bolsa deste hemocomponente pode sair agora'." |
| Volte para Concentrado de Hemácias | "As hemácias podem sair, e a fila vem por FEFO: vence primeiro, sai primeiro." |

**Cena 9 · Envio e rastreio (HU‑07) — 1 min**

| Na tela | Fala |
| :--- | :--- |
| Mantenha `CH-1042` → **Confirmar envio para Hospital das Clínicas de PE** | "A bolsa que vence em 3 dias sai primeiro. Ela passa para 'em trânsito', com a chegada prevista pela rota." |
| Mostre a linha do tempo | "A linha do tempo mostra origem, trânsito, chegada estimada e destino. As maletas não têm GPS: o progresso é estimado pelo tempo da rota." |
| Menu **Início** | "Bolsa em trânsito não conta mais no estoque: caímos para 6 unidades e 2 vencendo." |
| Menu **Rede** → **Confirmar recebimento** | "Quando a bolsa chega, o médico confirma o recebimento." |
| Menu **Estoque** → filtro **O-** | "E ela fica rastreada: `LOTE-SIM-0001`, entregue no Hospital das Clínicas de PE." |

**Encerramento (15 s):**

> "Do cadastro da bolsa à entrega no hospital, com a doação agendada no caminho: esse é o MVP do Rota Vital."

<h2 id="5-explicacao-do-codigo">5. Explicação do código</h2>

A disciplina é POO: em cada bloco, mostre o caminho da tela até o banco e destaque o conceito da última coluna.
Caminhos de Java a partir de `backend/src/main/java/com/rotavital/`.

<h3>Entrega 03</h3>

**Bloco A · Login**

| Abrir | Mostrar | Dizer |
| :--- | :--- | :--- |
| [`PaginaLogin.tsx`](../frontend/src/pages/Login/PaginaLogin.tsx) | `handleEntrar`, `avisoDoErro` | A tela só envia as credenciais e mostra o aviso |
| `api/AcessoController.java` | `entrar` | Recebe o HTTP, delega ao serviço e, pelo perfil, devolve a tela inicial e o menu |
| `servico/AutenticacaoService.java` | `entrar` | Valida os campos, confere a senha e liga o login ao usuário do banco |
| `servico/VerificadorDeCredenciais.java` e `supabase/SupabaseAuth.java` | `verificar` | **Interface:** o serviço não conhece o Supabase, e o teste troca a implementação |

**Bloco B · Estoque**

| Abrir | Mostrar | Dizer |
| :--- | :--- | :--- |
| `api/EstoqueController.java` | `GET` e `POST /hemocomponentes` | Listagem com filtros e cadastro |
| `servico/EstoqueService.java` | `validarNovaBolsa`, `cadastrarBolsa` | Regras de validade, volume e temperatura; o id da bolsa é gerado aqui |
| `dominio/BolsaHemocomponente.java` | `estaAptaParaUso`, `alterarStatus` | **Encapsulamento:** o status só muda pelo método, que recusa transição inválida |
| `api/TratadorDeErros.java` | `@ExceptionHandler` | **Exceções próprias** viram 400, 404 e 422 num lugar só |
| [`PaginaEstoque.tsx`](../frontend/src/pages/Estoque/PaginaEstoque.tsx) e [`ModalNovaBolsa.tsx`](../frontend/src/components/estoque/ModalNovaBolsa.tsx) | `listarHemocomponentes`, o formulário | Os filtros rodam no backend; a tela mostra o erro da API no formulário |

<h3>Entrega 04</h3>

**Bloco C · Campanhas e agendamentos**

| Abrir | Mostrar | Dizer |
| :--- | :--- | :--- |
| `servico/CampanhaService.java` | `publicar`, `encerrar` | Paciente e campanha na mesma transação; encerrar cancela os agendamentos em aberto |
| `servico/AgendamentoService.java` | `agendar`, `mudarStatus` | Um agendamento em aberto por doador |
| `dominio/AgendaDoHemocentro.java` | `horariosDo`, `VAGAS_POR_HORARIO` | **Regra de negócio no domínio:** a agenda é uma classe, não um `if` no controller |
| `servico/IdentificacaoService.java` | `exigir` | Quem age vem do header `X-Usuario-Id`, e cada ação exige um perfil |

**Bloco D · Como chegar**

| Abrir | Mostrar | Dizer |
| :--- | :--- | :--- |
| [`useGeolocalizacao.ts`](../frontend/src/hooks/useGeolocalizacao.ts) | A permissão e os erros | A posição só é lida com a permissão do doador, e cada falha tem mensagem |
| `servico/RotaAteHospitalService.java` | `calcular`, `orElseGet` | Tenta o serviço de mapas e, sem resposta, cai no plano B |
| `servico/CalculadoraDeTrajeto.java` e `google/GoogleRoutes.java` | `calcular`, retorno `Optional` | **Polimorfismo:** o serviço depende da interface, e o Google é só uma implementação |
| `dominio/Trajeto.java` | `estimadoEntre` | O plano B: linha reta × 1,3 a 25 km/h |

**Bloco E · Rede e envio**

| Abrir | Mostrar | Dizer |
| :--- | :--- | :--- |
| `dominio/PontoDeRede.java`, `PontoDeRedeBase.java`, `Hospital.java` e `BancoDeSangue.java` | A hierarquia | **Interface, classe abstrata e herança:** o grafo trata hospital e banco como pontos da rede |
| `dominio/RedeDistribuicao.java` | `calcularRotaMinima` | **Dijkstra** com `PriorityQueue`, guardando distância e predecessor |
| `servico/TransferenciaService.java` | `despachar`, `receber` | Só o médico despacha; a bolsa é conferida antes de chamar o Google, para não gastar cota |
| `dominio/BolsaHemocomponente.java` | `conferirEnvio`, `despacharPara` | **Encapsulamento de novo:** a própria bolsa diz se pode sair |
| [`useTransferencias.ts`](../frontend/src/hooks/useTransferencias.ts) | `INTERVALO_DE_ATUALIZACAO_MS` | A lista consulta a API a cada 15 s |

<h2 id="6-apresentacao-final">6. Apresentação final</h2>

São até **8 minutos**, de 09 a 13/11 (no dia da aula), com **todos os integrantes presentes**. Os tópicos abaixo são os
pedidos no enunciado. Ensaie com cronômetro: é fácil a demonstração tomar o tempo do resto.

| Tempo | Tópico | Exemplo do que dizer |
| :---: | :--- | :--- |
| 0:00 | **O problema** | Hemocomponente vence, exige temperatura certa e compatibilidade com o paciente. Estoque, transporte e busca por doadores costumam andar separados. Se quiserem abrir com um número, usem um dado real e com fonte |
| 1:00 | **A solução** | Para médicos de hospitais e hemocentros e para doadores voluntários. Histórias: login por perfil, estoque, campanhas, agendamento, rede e envio de bolsas. **Diferencial:** une doador e logística no mesmo sistema, com FEFO, cadeia fria e rastreio da bolsa |
| 2:30 | **Fluxo de trabalho** | Protótipo no Figma, histórias e tarefas no Jira, branch e pull request por tarefa (31 PRs e 189 commits até aqui), TDD nas regras críticas e esteira no GitHub Actions |
| 4:00 | **Ferramentas (com links)** | [Repositório](https://github.com/viictorpaes/Rotavital), [protótipo no Figma](https://letter-raven-07178463.figma.site), Jira, Java 21 e Spring Boot, React e Vite, PostgreSQL e Supabase, Docker Compose, GitHub Actions, Google Maps Platform |
| 4:45 | **Lições aprendidas** | Ver os exemplos abaixo |
| 5:45 | **Demonstração** | Vídeo curto de até 1 min 45: cena 3, cena 5 e cenas 8 e 9, sem as narrações longas |
| 7:30 | **Encerramento** | O que ficou pronto, o que vem depois e o agradecimento |

**Lições que o projeto rendeu de verdade:**

- **Teste verde não é sistema funcionando:** os 334 testes do `mvn package` passavam, e o ensaio com o sistema rodando
  ainda achou dois bugs: o aviso de perfil do doador e o 500 do diagnóstico.
- **Documento sozinho não garante nada:** o README prometia "FEFO + ABO/Rh", e o teste mostrou que o código só aceitava
  bolsa do mesmo tipo ([jornada TDD](../README.md#cdt-jornada)).
- **Ver o teste falhar:** nos 9 pares `test:` → `feat:`, o teste falhou pelo motivo certo antes do código existir.
- **Processo também precisa de manutenção:** as issues do GitHub ficaram paradas desde setembro enquanto o trabalho andava
  no Jira.
- **Com IA, o grupo confere:** os erros dela só apareceram rodando, no container e na esteira.

**Se perguntarem o que vem depois:** gravar requisição, recebimento e conclusão de procedimento, conferir o token do
Supabase na API e publicar em produção.

<h2 id="7-o-que-nao-mostrar">7. O que não mostrar</h2>

Estes pontos funcionam só na aparência em 06/11/2026. Se aparecerem na gravação, não os apresente como reais.

| Na tela | O que parece | O que acontece | O que fazer |
| :--- | :--- | :--- | :--- |
| Requisição › **Solicitar** | "Requisição registrada", "Lote reservado" | Nada é gravado nem reservado | Não preencha: vá direto para a aba Campanha |
| Requisição › **Recebidas** | Soma remessas ao estoque | As remessas são de exemplo e voltam com F5 | Não abra |
| Pacientes › **Concluir procedimento** | Recibo com baixa no estoque | Com F5, o paciente volta para a fila | Não clique |
| Admin › **Diagnóstico** | "Back-end Online", "45 ms", "4 pontos" | `/diagnostico` responde 500 e a tela mostra valores de reserva | Não use como prova de conexão |
| Estoque › selo **Supabase conectado** | Banco na nuvem | O texto é fixo: aparece também com o banco local | Não leia em voz alta |
| Rede › **Veículo refrigerado monitorado** | Telemetria ao vivo | Texto fixo: não há GPS nem sensor | Diga "estimado pelo tempo da rota" |

Também não prometa edição e descarte de bolsa ([PR #29](https://github.com/viictorpaes/Rotavital/pull/29), ainda fora da
`main`) nem endereço público: a produção ainda não foi ligada.

<h2 id="8-se-algo-der-errado">8. Se algo der errado</h2>

| Sintoma | Causa | Saída |
| :--- | :--- | :--- |
| Aviso "Não foi possível carregar o seu perfil" | Bug conhecido: acontece em todo login de doador | F5 |
| Campanha ou agendamento novo não aparece | Os dados carregam ao abrir a tela | F5 |
| Painel com número antigo depois da Nova bolsa | Ele só relê o estoque ao carregar ou depois de um envio | F5 |
| "Você já tem uma doação agendada" | Um agendamento em aberto por doador | **Cancelar** em Minhas doações ou zerar o banco |
| Modal "Doação agendada" não fecha com Esc | O modal só fecha pelo botão | **Fechar** |
| Fila FEFO vazia em Hemácias | As bolsas foram usadas em tentativas anteriores | Zerar o banco |
| "Não foi possível conferir o login agora" | Sem internet, sem `SUPABASE_KEY` ou Supabase fora do ar | Confira o `.env` e a rede |
| Site não sobe depois de atualizar o código | Volume antigo, sem as migrations novas | `npm run docker:down:v` e `npm run docker:local` |

**Plano B para a apresentação:** leve a demonstração em vídeo. Ela não depende da internet da sala.
