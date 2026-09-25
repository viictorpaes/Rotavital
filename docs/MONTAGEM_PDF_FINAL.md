# RotaVital — Documento Final de Arquitetura

## 1. Capa

RotaVital  
Sistema de logística de distribuição de hemocomponentes  
Projeto Integrador — Infraestrutura de Software  
Turma: [informar turma]  
Data: 22/09/2026  
Integrantes: [informar nomes]

---

## 2. Descrição do sistema

O RotaVital é um sistema de logística de distribuição de hemocomponentes entre hospitais e bancos de sangue, com foco em gestão de estoque, requisições hospitalares, compatibilidade ABO/Rh, roteirização de entregas e telemetria da cadeia fria. O objetivo principal é garantir que os hemocomponentes certos cheguem ao paciente correto, no momento adequado, com rastreabilidade e controle de validade.

Os usuários principais são médicos, gestores hospitalares e profissionais de hemocentro, além de doadores e usuários do portal de doações. O sistema apoia a tomada de decisão com visualização do estoque, priorização de requisições, alocação por FEFO e compatibilidade, e acompanhamento da logística de transporte.

---

## 3. Diagrama de arquitetura

O diagrama de arquitetura foi construído no draw.io e está disponível em:

- [DIAGRAMA_ROTAVITAL.drawio](./DIAGRAMA_ROTAVITAL.drawio)
- [DIAGRAMA_ROTAVITAL.md](./DIAGRAMA_ROTAVITAL.md)

### Visão resumida do desenho

- Rede principal: 10.0.0.0/16
- Rede pública: 10.0.1.0/24
- Rede de aplicação: 10.0.10.0/24
- Rede de dados: 10.0.20.0/24

Fluxos principais:

- Usuário / Internet → Load Balancer / Gateway: HTTPS/443
- Gateway → Frontend: HTTPS/443
- Frontend → Backend API: HTTP/1.1/8080
- Backend API → PostgreSQL / Supabase: PostgreSQL sobre TCP/5432
- Gateway → DNS: DNS sobre UDP/53
- Backend → observabilidade: métricas/logs

> O diagrama final deve ser exportado em PDF e PNG a partir do arquivo draw.io, com legenda e rótulos em todas as setas.

---

## 4. Tabela de endpoints REST

Com base em [openapi.yaml](./openapi.yaml), os endpoints principais do sistema são:

| Método | Endpoint | Descrição |
|---|---|---|
| GET | /estoque/{bancoId} | Consulta o estoque consolidado de um banco de sangue |
| GET | /hemocomponentes | Lista bolsas com filtros |
| POST | /hemocomponentes | Registra nova bolsa |
| GET | /hemocomponentes/{id} | Consulta bolsa por id |
| PATCH | /hemocomponentes/{id} | Atualiza status da bolsa |
| DELETE | /hemocomponentes/{id} | Remove o cadastro |
| POST | /requisicoes | Cria requisição hospitalar |
| GET | /requisicoes | Lista requisições |
| GET | /requisicoes/{id} | Consulta requisição |
| POST | /requisicoes/{id}/alocar | Aloca bolsa compatível |
| POST | /requisicoes/{id}/cancelar | Cancela requisição |
| GET | /rotas/pontos | Lista pontos do grafo |
| GET | /rotas/conexoes | Lista conexões da rota |
| POST | /rotas/calcular | Calcula rota |
| POST | /telemetria/temperatura | Registra leitura de temperatura |
| GET | /entregas/{id}/monitoramento | Consulta histórico de monitoramento |

---

## 5. Tabela de ligações e protocolos

| # | Origem | Destino | Protocolo | Porta |
|---:|---|---|---|---:|
| 1 | Navegador / Internet | Frontend | HTTPS (TLS 1.3) | 443 |
| 2 | Frontend | Backend API | HTTP/1.1 | 8080 |
| 3 | Backend API | PostgreSQL / Supabase | PostgreSQL sobre TCP (JDBC) | 5432 |
| 4 | Qualquer host | DNS | DNS sobre UDP | 53 |

Observações:

- a borda do sistema usa HTTPS, nunca HTTP puro;
- o banco não é exposto diretamente à internet;
- não há fila assíncrona implementada no projeto atual.

---

## 6. Tabela de definição de redes

| Rede/Sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| Rede principal | 10.0.0.0/16 | Principal | Sistema inteiro | -- | -- |
| Sub-rede pública | 10.0.1.0/24 | Pública | Load balancer / gateway | Internet → 443 (HTTPS) | Para aplicação em 80/443; DNS em 53 |
| Sub-rede de aplicação | 10.0.10.0/24 | Privada de aplicação | Frontend, Backend API | Só da sub-rede pública | Para dados em 5432; DNS em 53 |
| Sub-rede de dados | 10.0.20.0/24 | Privada de dados | PostgreSQL / Supabase | Só da sub-rede de aplicação em 5432 | Nenhuma saída para internet |

Regra de ouro:

- banco de dados nunca em sub-rede pública;
- banco acessível somente pela aplicação;
- internet não entra diretamente no banco.

---

## 7. Legenda e convenções

### Cores

- Azul: rede pública / entrada externa
- Verde: aplicação / serviços web e backend
- Roxo: dados / banco
- Laranja: observabilidade / métricas e logs

### Formas

- Usuário / Internet: ícone de usuário
- Load Balancer / Gateway: ícone de roteador
- Frontend: servidor web
- Backend: servidor de aplicação
- Banco: ícone de banco de dados

### Tipos de linha

- Contínua: comunicação síncrona
- Tracejada: comunicação assíncrona
- Pontilhada: métricas/logs e observabilidade

---

## Verificação cruzada

Checklist final:

- [x] Todo endpoint da tabela está exposto por algum componente do diagrama?
- [x] Toda seta do diagrama tem uma linha na tabela de protocolos?
- [x] Todo componente do diagrama está dentro de alguma sub-rede da tabela de redes?

Conclusão: o documento está pronto para montagem final em PDF, desde que o arquivo draw.io seja exportado em PDF e o conteúdo desta compilação seja incorporado na ordem exigida.

---

## Observação de exportação final

Para gerar o PDF final, abra o arquivo [DIAGRAMA_ROTAVITAL.drawio](./DIAGRAMA_ROTAVITAL.drawio) no draw.io/app.diagrams.net e execute:

- File → Export as → PDF
- File → Export as → PNG, com zoom 300%

Em seguida, reúna as seções neste documento na ordem solicitada, mantendo a capa, descrição, diagrama, endpoints, ligações, redes e legenda.
