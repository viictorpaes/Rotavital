<h1 align="center">Evidências · PI3-14 · Endpoints REST do MVP</h1>

> Chamadas reais contra o backend (`mvn spring-boot:run`, porta `8080`), geradas em **23/09/2026 22:14** a partir do
> código-base `39507b9` com as mudanças da padronização `/api/v1`. Tabela de referência:
> [`CONTRATOS_DE_API.md`, seção 2.3](../CONTRATOS_DE_API.md#2-3-endpoints).

**Resultado:** 14/14 chamadas retornaram o status esperado.

| # | Recurso | Método | Caminho | Cenário | Esperado | Obtido | |
|---|---|---|---|---|---|---|---|
| 1 | Acessos | `POST` | `/api/v1/acessos` | Perfil médico válido | `200` | `200` | ✅ |
| 2 | Acessos | `POST` | `/api/v1/acessos` | Perfil doador válido | `200` | `200` | ✅ |
| 3 | Acessos | `POST` | `/api/v1/acessos` | Nome vazio e tipoAcesso ausente | `400` | `400` | ✅ |
| 4 | Bancos → Estoque | `GET` | `/api/v1/bancos/BS-01/estoque?tipoSanguineo=O_NEGATIVO` | Filtro por query string | `200` | `200` | ✅ |
| 5 | Bancos → Estoque | `GET` | `/api/v1/bancos/XX/estoque` | Banco inexistente | `404` | `404` | ✅ |
| 6 | Pontos | `GET` | `/api/v1/pontos` | Nós do grafo | `200` | `200` | ✅ |
| 7 | Conexões | `GET` | `/api/v1/conexoes` | Arestas do grafo | `200` | `200` | ✅ |
| 8 | Rotas | `GET` | `/api/v1/rotas?origemId=BS-01&destinoId=HOSP-01&janelaEntregaLimite=2026-12-31T21:00:00` | Rota mínima dentro da janela | `200` | `200` | ✅ |
| 9 | Rotas | `GET` | `/api/v1/rotas?origemId=BS-01` | destinoId ausente | `400` | `400` | ✅ |
| 10 | Rotas | `GET` | `/api/v1/rotas?origemId=BS-01&destinoId=NOPE` | Ponto de rede inexistente | `404` | `404` | ✅ |
| 11 | Benchmarks | `POST` | `/api/v1/benchmarks/auditoria-telemetria` | Auditoria sem persistência | `200` | `200` | ✅ |
| 12 | Planejado (só contrato) | `GET` | `/api/v1/hemocomponentes` | Sem controller ainda (🕓 na tabela) | `404` | `404` | ✅ |
| 13 | Migração | `GET` | `/estoque/BS-01` | URL antiga não existe mais | `404` | `404` | ✅ |
| 14 | Migração | `POST` | `/rotas/calcular` | URL antiga com verbo não existe mais | `404` | `404` | ✅ |

## Lint do contrato (Spectral, ruleset `spectral:oas`)

```text
$ npx @stoplight/spectral-cli lint docs/openapi.yaml
No results with a severity of 'error' found!
```

## Detalhe de cada chamada

### 1. POST /api/v1/acessos

Perfil médico válido · esperado `200` · obtido **`200`** ✅

```bash
curl -i -X POST 'http://localhost:8080/api/v1/acessos' \
  -H 'Content-Type: application/json' \
  -d '{"nome": "Dra. Ana", "tipoAcesso": "MEDICO"}'
```

```json
{
  "nome": "Dra. Ana",
  "tipoAcesso": "MEDICO",
  "telaInicial": "Painel Operacional",
  "menu": [
    "Início",
    "Estoque",
    "Requisição",
    "Rede",
    "Pacientes",
    "Doações"
  ]
}
```

### 2. POST /api/v1/acessos

Perfil doador válido · esperado `200` · obtido **`200`** ✅

```bash
curl -i -X POST 'http://localhost:8080/api/v1/acessos' \
  -H 'Content-Type: application/json' \
  -d '{"nome": "João", "tipoAcesso": "DOADOR"}'
```

```json
{
  "nome": "João",
  "tipoAcesso": "DOADOR",
  "telaInicial": "Portal do Doador",
  "menu": []
}
```

### 3. POST /api/v1/acessos

Nome vazio e tipoAcesso ausente · esperado `400` · obtido **`400`** ✅

```bash
curl -i -X POST 'http://localhost:8080/api/v1/acessos' \
  -H 'Content-Type: application/json' \
  -d '{"nome": ""}'
```

```json
{
  "type": "about:blank",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "Informe o nome e o tipo de acesso para entrar",
  "instance": "/api/v1/acessos"
}
```

### 4. GET /api/v1/bancos/BS-01/estoque?tipoSanguineo=O_NEGATIVO

Filtro por query string · esperado `200` · obtido **`200`** ✅

```bash
curl -i -X GET 'http://localhost:8080/api/v1/bancos/BS-01/estoque?tipoSanguineo=O_NEGATIVO'
```

```json
{
  "bancoDeSangueId": "BS-01",
  "totalBolsas": 3,
  "bolsas": [
    {
      "id": "CH-1042",
      "tipoComponente": "HEMACIAS",
      "tipoSanguineo": "O_NEGATIVO",
      "dataColeta": "2026-08-22",
      "dataValidade": "2026-09-26",
      "loteSintetico": "LOTE-SIM-0001",
      "volumeMl": 450.0,
      "temperaturaCelsius": 4.0,
      "localizacao": "R1 · P3 · N2",
      "foraDaFaixa": false,
      "status": "DISPONIVEL",
      "bancoOrigemId": "BS-01"
    },
    {
      "id": "CH-1043",
      "tipoComponente": "HEMACIAS",
      "tipoSanguineo": "O_NEGATIVO",
      "dataColeta": "2026-09-08",
      "dataValidade": "2026-10-13",
      "loteSintetico": "LOTE-SIM-0002",
      "volumeMl": 450.0,
      "temperaturaCelsius": 3.5,
      "localizacao": "R1 · P3 · N3",
      "foraDaFaixa": false,
      "status": "DISPONIVEL",
      "bancoOrigemId": "BS-01"
    },
    {
      "id": "CR-4002",
      "tipoComponente": "CRIOPRECIPITADO",
      "tipoSanguineo": "O_NEGATIVO",
      "dataColeta": "2026-09-13",
      "dataValidade": "2027-09-13",
      "loteSintetico": "LOTE-SIM-0003",
      "volumeMl": 30.0,
      "temperaturaCelsius": -25.0,
      "localizacao": "F2 · P1 · N1",
      "foraDa
… (truncado)
```

### 5. GET /api/v1/bancos/XX/estoque

Banco inexistente · esperado `404` · obtido **`404`** ✅

```bash
curl -i -X GET 'http://localhost:8080/api/v1/bancos/XX/estoque'
```

```json
{
  "type": "about:blank",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Nenhum banco de sangue encontrado com id XX",
  "instance": "/api/v1/bancos/XX/estoque"
}
```

### 6. GET /api/v1/pontos

Nós do grafo · esperado `200` · obtido **`200`** ✅

```bash
curl -i -X GET 'http://localhost:8080/api/v1/pontos'
```

```json
[
  {
    "id": "BS-01",
    "nome": "Hemope Central",
    "tipo": "BANCO_DE_SANGUE",
    "latitude": -8.0578,
    "longitude": -34.8829
  },
  {
    "id": "HOSP-01",
    "nome": "Hospital das Clinicas",
    "tipo": "HOSPITAL",
    "latitude": -8.0476,
    "longitude": -34.877
  },
  {
    "id": "hc-pe",
    "nome": "Hospital das Clínicas de PE",
    "tipo": "HOSPITAL",
    "latitude": -8.0512,
    "longitude": -34.9478
  },
  {
    "id": "real-portugues",
    "nome": "Real Hospital Português",
    "tipo": "HOSPITAL",
    "latitude": -8.0479,
    "longitude": -34.8993
  },
  {
    "id": "barao-lucena",
    "nome": "Hospital Barão de Lucena",
    "tipo": "HOSPITAL",
    "latitude": -8.0432,
    "longitude": -34.9331
  },
  {
    "id": "getulio-vargas",
    "nome": "Hospital Getúlio Vargas",
    "tipo": "HOSPITAL",
    "latitude": -8.0812,
    "longitude": -34.9127
  },
  {
    "id": "upa-norte",
    "nome": "UPA Norte - Macaxeira",
    "tipo": "HOSPITAL",
    "latitude": -8.0098,
    "longitude": -34.9296
  }
]
```

### 7. GET /api/v1/conexoes

Arestas do grafo · esperado `200` · obtido **`200`** ✅

```bash
curl -i -X GET 'http://localhost:8080/api/v1/conexoes'
```

```json
[
  {
    "origemId": "BS-01",
    "destinoId": "HOSP-01",
    "distanciaKm": 3.2,
    "tempoEstimadoMin": 9.0
  },
  {
    "origemId": "HOSP-01",
    "destinoId": "BS-01",
    "distanciaKm": 3.2,
    "tempoEstimadoMin": 9.0
  },
  {
    "origemId": "BS-01",
    "destinoId": "hc-pe",
    "distanciaKm": 7.8,
    "tempoEstimadoMin": 18.0
  },
  {
    "origemId": "hc-pe",
    "destinoId": "BS-01",
    "distanciaKm": 7.8,
    "tempoEstimadoMin": 18.0
  },
  {
    "origemId": "BS-01",
    "destinoId": "real-portugues",
    "distanciaKm": 2.1,
    "tempoEstimadoMin": 7.0
  },
  {
    "origemId": "real-portugues",
    "destinoId": "BS-01",
    "distanciaKm": 2.1,
    "tempoEstimadoMin": 7.0
  },
  {
    "origemId": "BS-01",
    "destinoId": "barao-lucena",
    "distanciaKm": 1.9,
    "tempoEstimadoMin": 6.0
  },
  {
    "origemId": "barao-lucena",
    "destinoId": "BS-01",
    "distanciaKm": 1.9,
    "tempoEstimadoMin": 6.0
  },
  {
    "origemId": "BS-01",
    "destinoId": "getulio-vargas",
    "distanciaKm": 7.1,
    "tempoEstimadoMin": 16.0
  },
  {
    "origemId": "getulio-vargas",
    "destinoId": "BS-01",
    "distanciaKm": 7.1,
    "tempoEstimadoMin": 16.0
  },
  {
    "origemId": 
… (truncado)
```

### 8. GET /api/v1/rotas?origemId=BS-01&destinoId=HOSP-01&janelaEntregaLimite=2026-12-31T21:00:00

Rota mínima dentro da janela · esperado `200` · obtido **`200`** ✅

```bash
curl -i -X GET 'http://localhost:8080/api/v1/rotas?origemId=BS-01&destinoId=HOSP-01&janelaEntregaLimite=2026-12-31T21:00:00'
```

```json
{
  "origemId": "BS-01",
  "destinoId": "HOSP-01",
  "nos": [
    "BS-01",
    "HOSP-01"
  ],
  "distanciaTotalKm": 3.2,
  "tempoEstimadoMin": 9.0,
  "dentroDaJanela": true
}
```

### 9. GET /api/v1/rotas?origemId=BS-01

destinoId ausente · esperado `400` · obtido **`400`** ✅

```bash
curl -i -X GET 'http://localhost:8080/api/v1/rotas?origemId=BS-01'
```

```json
{
  "type": "about:blank",
  "title": "Requisição inválida",
  "status": 400,
  "detail": "Informe origemId e destinoId na query string",
  "instance": "/api/v1/rotas"
}
```

### 10. GET /api/v1/rotas?origemId=BS-01&destinoId=NOPE

Ponto de rede inexistente · esperado `404` · obtido **`404`** ✅

```bash
curl -i -X GET 'http://localhost:8080/api/v1/rotas?origemId=BS-01&destinoId=NOPE'
```

```json
{
  "type": "about:blank",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Ponto de rede não encontrado: NOPE",
  "instance": "/api/v1/rotas"
}
```

### 11. POST /api/v1/benchmarks/auditoria-telemetria

Auditoria sem persistência · esperado `200` · obtido **`200`** ✅

```bash
curl -i -X POST 'http://localhost:8080/api/v1/benchmarks/auditoria-telemetria' \
  -H 'Content-Type: application/json' \
  -d '{"quantidadeRegistros": 1000, "modo": "SEQUENCIAL"}'
```

```json
{
  "tamanhoMassa": 1000,
  "todosResultadosIdenticos": true,
  "mensagemConsistencia": "SUCESSO: Todas as versões (sequencial e paralelas) retornaram exatamente o mesmo resultado numérico. Zero race condition!",
  "execucoes": [
    {
      "modo": "SEQUENCIAL",
      "threads": 1,
      "tempoExecucaoMs": 2,
      "speedup": 1.0,
      "totalProcessado": 1000,
      "anomaliasDetectadas": 273,
      "lotesCriticosDescarte": 24,
      "mediaTemperatura": 1.361,
      "desvioPadraoTemperatura": 17.352,
      "indiceDegradacaoMedio": 15.608
    }
  ]
}
```

### 12. GET /api/v1/hemocomponentes

Sem controller ainda (🕓 na tabela) · esperado `404` · obtido **`404`** ✅

```bash
curl -i -X GET 'http://localhost:8080/api/v1/hemocomponentes'
```

```json
{
  "timestamp": "2026-09-24T01:14:33.146+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/v1/hemocomponentes"
}
```

### 13. GET /estoque/BS-01

URL antiga não existe mais · esperado `404` · obtido **`404`** ✅

```bash
curl -i -X GET 'http://localhost:8080/estoque/BS-01'
```

```json
{
  "timestamp": "2026-09-24T01:14:33.150+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/estoque/BS-01"
}
```

### 14. POST /rotas/calcular

URL antiga com verbo não existe mais · esperado `404` · obtido **`404`** ✅

```bash
curl -i -X POST 'http://localhost:8080/rotas/calcular' \
  -H 'Content-Type: application/json' \
  -d '{"origemId": "BS-01", "destinoId": "HOSP-01"}'
```

```json
{
  "timestamp": "2026-09-24T01:14:33.155+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/rotas/calcular"
}
```
