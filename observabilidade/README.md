# Observabilidade do Rota Vital (RSD)

Projeto de Avaliação de **Infraestrutura de Comunicação**, sobre observabilidade com Actuator, Prometheus e Grafana (Prof. Luiz Novaes).

Este projeto adiciona ao sistema do `compose.yaml` os três pilares da observabilidade, todos visíveis num só Grafana:

| Pilar | Quem gera | Quem guarda | Como chega lá |
| :--- | :--- | :--- | :--- |
| **Métricas** | Spring Boot Actuator + Micrometer (`/actuator/prometheus`), Node Exporter, cAdvisor | Prometheus | O Prometheus faz o *scrape* a cada 15 s |
| **Logs** | stdout dos contêineres, com `traceId` em cada linha | Loki | O Grafana Alloy lê os logs pelo `docker.sock` |
| **Traces** | Micrometer Tracing (OpenTelemetry) com um span por requisição e por consulta JDBC | Tempo | Enviados por OTLP/HTTP na porta 4318 |

O Grafana liga as três fontes. Do log se chega ao trace (clicando no `traceId`) e do trace se volta aos logs da mesma requisição.

## Pré-requisitos

- **Docker Desktop**, ou Docker Engine com o plugin Compose v2.
- **Node.js**, só para os atalhos `npm run obs:*`. Sem ele, use os comandos `docker compose` mostrados abaixo.
- Um arquivo `.env` na raiz do projeto, copiado de `.env.example`. Além de `DB_PASSWORD`, ele precisa de:

```dotenv
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=<senha forte do admin>
GRAFANA_VIEWER_USER=professor
GRAFANA_VIEWER_PASSWORD=<senha do usuário Viewer>
```

O `.env` está no `.gitignore`. Nenhuma senha fica no repositório, e o compose recusa subir se `GRAFANA_ADMIN_PASSWORD` ou `GRAFANA_VIEWER_PASSWORD` estiverem vazias.

## Como executar

```bash
npm run obs:up       # docker compose -f compose.yaml -f compose.observabilidade.yaml up -d --build
npm run obs:status   # estado dos serviços (ps)
npm run obs:url      # URL pública do Grafana (logs do cloudflared)
npm run obs:logs     # logs de todos os serviços
npm run obs:down     # derruba tudo (os volumes ficam)
```

O primeiro `up` demora alguns minutos, porque compila o backend e baixa as imagens. O gerador de carga começa sozinho assim que o backend fica saudável.

## Portas e URLs

| Serviço | Endereço | Exposição |
| :--- | :--- | :--- |
| Sistema (frontend + `/api`) | http://localhost:8080 | Porta do sistema, já existente |
| **Grafana** | http://localhost:3000 | Só em `127.0.0.1` e **pelo túnel do Cloudflare** (o único serviço público) |
| Prometheus | http://localhost:9090 (alvos em `/targets`) | Só em `127.0.0.1` |
| Actuator do backend | `backend:8081/actuator/*` | **Sem porta publicada**: só a rede interna do Docker alcança |
| Loki, Tempo, Alloy, Node Exporter, cAdvisor | `loki:3100`, `tempo:3200`/`4318`, `node-exporter:9100`, `cadvisor:8080` | Só na rede interna |

Para ver o `/actuator/prometheus` de dentro da rede:

```bash
docker compose -f compose.yaml -f compose.observabilidade.yaml exec backend \
  wget -qO- http://localhost:8081/actuator/prometheus | grep rotavital_
```

## Jobs do Prometheus

O arquivo é [`prometheus/prometheus.yml`](./prometheus/prometheus.yml). Todos os alvos devem aparecer **UP** em `/targets`.

| Job | Alvo | O que mede |
| :--- | :--- | :--- |
| `rotavital-backend` | `backend:8081/actuator/prometheus` | HTTP, JVM, Hikari e métricas de negócio |
| `node-exporter` | `node-exporter:9100` | CPU, memória, disco, rede e load do host |
| `cadvisor` | `cadvisor:8080` | CPU, memória e disco de cada contêiner |
| `prometheus` | `localhost:9090` | O próprio Prometheus |
| `loki` | `loki:3100` | Saúde do Loki |
| `tempo` | `tempo:3200` | Saúde do Tempo |

Todas as métricas do backend levam a tag `application="rotavital-backend"`, configurada em `management.metrics.tags.application`.

## Dashboards

Os dashboards ficam versionados em [`grafana/dashboards/`](./grafana/dashboards/) e são provisionados na pasta **Rota Vital**. As fontes de dados (Prometheus, Loki e Tempo) vêm de [`grafana/provisioning/`](./grafana/provisioning/).

- **Rota Vital · Aplicação** (`rotavital-aplicacao.json`), o dashboard inicial:
  - indicadores do topo: backend no ar, uptime, req/s, p95, 4xx/s e 5xx/s;
  - linha HTTP: requisições por URI, latência p50/p95/p99 e status;
  - linha JVM: heap, GC, threads e CPU;
  - linha Hikari: conexões do pool e tempo de aquisição;
  - linha de negócio;
  - painel de logs do backend.
- **Rota Vital · Infraestrutura** (`rotavital-infraestrutura.json`):
  - medidores de CPU, memória e disco do host;
  - CPU por modo, memória, rede e load average;
  - CPU, memória e disco por contêiner (cAdvisor);
  - `up` de cada job.

## Métricas de negócio (Micrometer)

As métricas são definidas em [`MetricasDeNegocio.java`](../backend/src/main/java/com/rotavital/observabilidade/MetricasDeNegocio.java) e cada uma tem painel próprio no dashboard de Aplicação.

| Métrica no Prometheus | Tipo | Tags | Onde é registrada | O que responde |
| :--- | :--- | :--- | :--- | :--- |
| `rotavital_rotas_calculo_seconds` | Timer (histograma) | `resultado` = `ok`, `fora_da_janela`, `nao_encontrado` ou `sem_rota` | `GET /api/v1/rotas` | Quantas rotas de entrega são calculadas por minuto, quanto o Dijkstra demora (média e p95) e quantas saem fora da janela de transporte |
| `rotavital_lotes_sugeridos_total` | Counter | `componente`, `atendimento` = `completo` ou `parcial` | `GET /api/v1/bancos/{id}/sugestao-lote` | Quantas sugestões de lote FEFO atenderam o pedido inteiro e quantas ficaram faltando bolsas |
| `rotavital_bolsas_cadastradas_total` | Counter | `componente` | `POST /api/v1/hemocomponentes` | Entrada de bolsas no estoque, por hemocomponente |
| `rotavital_bolsas_descartadas_total` | Counter | `categoria` (motivo) | `PATCH /api/v1/hemocomponentes/{id}` (descarte) | Perdas do estoque, por motivo |

Exemplos de PromQL:

```promql
# rotas calculadas por minuto, por resultado
sum by (resultado) (rate(rotavital_rotas_calculo_seconds_count[5m])) * 60

# p95 do cálculo de rota
histogram_quantile(0.95, sum by (le) (rate(rotavital_rotas_calculo_seconds_bucket[5m])))

# % de sugestões de lote atendidas por completo
sum(rate(rotavital_lotes_sugeridos_total{atendimento="completo"}[15m]))
  / sum(rate(rotavital_lotes_sugeridos_total[15m]))
```

## Logs e traces

- **Logs:** o Alloy ([`alloy/config.alloy`](./alloy/config.alloy)) descobre os contêineres pelo `docker.sock` e manda os logs ao Loki com os labels `container`, `service` e `application`.
  - Consulta de exemplo: `{application="rotavital-backend"} |= "traceId"`.
- **Traces:** cada requisição HTTP vira um trace no Tempo, com um span do controller e um span por consulta JDBC (`datasource-micrometer`).
  - O cálculo de rota, por exemplo, mostra a requisição e as consultas da rede e das conexões.
- **Correlação:** o compose define `LOGGING_PATTERN_CORRELATION`, que põe `[traceId=… spanId=…]` em cada linha.
  - O datasource Loki tem um *derived field* que transforma o `traceId` num link para o Tempo.
  - O Tempo tem *trace to logs* de volta ao Loki.

## Acesso remoto (R7)

- O serviço `cloudflared` abre um *quick tunnel* do Cloudflare apontando para `grafana:3000`. A URL, no formato `https://<nome>.trycloudflare.com`, aparece em `npm run obs:url`.
  - Ela muda se o contêiner do túnel for recriado. A URL válida é a informada no PDF e no Classroom.
- O serviço `grafana-usuario-professor` cria o usuário **Viewer** com `GRAFANA_VIEWER_USER` e `GRAFANA_VIEWER_PASSWORD`, lidos do `.env`.
  - As credenciais só são entregues no PDF e no Classroom, nunca no repositório.
- O `gerador-de-carga` ([`carga/gerar-carga.sh`](./carga/gerar-carga.sh)) fica rodando o tempo todo para os painéis terem dados:
  - consultas, cálculos de rota e sugestões de lote a cada 2 s;
  - uma bolsa nova cadastrada a cada 60 ciclos (uns 3 min);
  - a cada 3 ciclos, casos de erro (400 e 404).

## Segurança (R5)

- O `/actuator` fica numa porta própria (8081), sem mapeamento para o host. A porta 8082 de depuração da API não responde `/actuator`.
- Só `health`, `info`, `metrics` e `prometheus` são expostos, e o `health` não mostra detalhes.
- Fora deste compose (no `docker compose up` normal, no Render e nos testes), nada muda em relação à `main`:
  - o backend continua expondo só `health` e `info`;
  - o tracing e os spans de JDBC ficam desligados.

  Quem liga tudo isso são as variáveis `ACTUATOR_EXPOSICAO`, `MANAGEMENT_SERVER_PORT`, `TRACING_ENABLED` e `JDBC_OBSERVACAO`, definidas em `compose.observabilidade.yaml`.
- O Grafana tem senha de admin obrigatória vinda do `.env`. Cadastro e acesso anônimo estão desligados, os usuários novos entram como Viewer, e o papel Viewer pode usar o Explore (`GF_USERS_VIEWERS_CAN_EDIT=true`) para inspecionar logs/traces sem salvar alterações nos dashboards.
- Grafana e Prometheus só escutam em `127.0.0.1`. Para fora, só sai o Grafana, pelo túnel.

## Observação sobre o Docker Desktop

No Windows e no macOS, o Docker roda dentro de uma VM. Por isso, o Node Exporter e o cAdvisor medem **a VM do Docker Desktop**, e não o sistema operacional da máquina.

Os números de CPU, memória e disco do dashboard de Infraestrutura são os dessa VM. Isso é esperado e está explicado no relatório.

### cAdvisor com Docker 29

A partir do Docker 29, as imagens ficam no *image store* do containerd. O modo Docker do cAdvisor (v0.49) ainda procura as camadas no layout antigo e descarta os contêineres, com o erro `failed to identify the read-write layer ID` ([google/cadvisor#3643](https://github.com/google/cadvisor/issues/3643)).

Por isso, o compose sobe o cAdvisor **sem o `docker.sock`**. Assim ele lê os cgroups diretamente (`/docker/<id>`) e entrega CPU, memória e disco de cada contêiner, identificados pelo **id curto**, o mesmo que aparece em `docker ps`.

