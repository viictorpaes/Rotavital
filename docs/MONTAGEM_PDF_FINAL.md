# RotaVital — Documento Final de Arquitetura

## 1. Capa

RotaVital  
Sistema de logística de distribuição de hemocomponentes  
Projeto Integrador — Infraestrutura de Software  
Turma/equipe: Equipe 1 - Rota Vital  
Data: 26/09/2026  
Integrantes: Victor José Paes e Silva; Eduardo de Souza Cavalcanti Junior; Felipe Franca Alves de Lima; Helamã Leone de Lima Procídio; João Pedro Arruda Guimarães; Lucas Paguetti Pereira; Tiago Luiz Moreira de Vasconcelos

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
- Sub-rede privada de aplicação: 10.0.10.0/24
- Sub-rede privada de dados: 10.0.20.0/24

Fluxos principais:

- Usuário / Internet → Load Balancer / Gateway: HTTPS/443
- Gateway → Frontend (Nginx): HTTP/1.1/80 (rede privada)
- Frontend → Backend API: HTTP/1.1/8080
- Backend API → PostgreSQL autogerenciado (sub-rede de dados): TCP (PostgreSQL)/5432
- Backend API → DNS Resolver/Forwarder privado na sub-rede de aplicação: DNS/UDP/53

> O diagrama final deve ser exportado em PDF e PNG a partir do arquivo draw.io, com legenda e rótulos em todas as setas.
> O Load Balancer com TLS, o NAT Gateway e as sub-redes são arquitetura-alvo; o Compose atual não os configura. O Supabase usado pelo projeto é externo e gerenciado, não o banco autogerenciado mostrado na sub-rede privada de dados.

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
| 1 | Navegador / Internet | Load Balancer / Gateway | HTTPS (TLS 1.3) | 443 |
| 2 | Load Balancer / Gateway | Frontend (Nginx) | HTTP/1.1 | 80 |
| 3 | Frontend (Nginx) | Backend API | HTTP/1.1 | 8080 |
| 4 | Backend API | PostgreSQL / Supabase | TCP (PostgreSQL/JDBC, TLS) | 5432 |
| 5 | Backend API | DNS Resolver | DNS sobre UDP | 53 |
| 6 | Backend API (publicador) | RabbitMQ (fila/eventos) | AMQP 0-9-1 sobre TCP | 5672 |
| 7 | RabbitMQ (fila/eventos) | Worker assíncrono (consumidor) | AMQP 0-9-1 sobre TCP | 5672 |

Observações:

- a conexão vinda da internet termina no gateway por HTTPS; HTTP/1.1/80 ocorre somente entre gateway e Nginx na rede privada;
- na arquitetura-alvo, o banco não é exposto diretamente à internet (no projeto atual, o Supabase é um serviço externo gerenciado);
- as ligações assíncronas propostas são Backend → RabbitMQ (publicação) e RabbitMQ → Worker (consumo), ambas AMQP 0-9-1/TCP/5672 e tracejadas no diagrama;
- RabbitMQ e Worker são componentes da arquitetura proposta para representar a mensageria assíncrona; não estão implementados nem configurados no Compose atual;
- o Compose atual publica as portas do host 80 (Frontend) e 8080 (API), sem gateway/TLS; HTTPS na borda e restrição de acesso direto à API dependem da configuração de produção.

---

## 6. Tabela de definição de redes

| Rede/Sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| Rede principal (VPC) | 10.0.0.0/16 | Rede privada virtual | Sub-redes pública, de aplicação e de dados | Regras aplicadas nas sub-redes | Conforme regras de cada sub-rede |
| Sub-rede pública | 10.0.1.0/24 | Pública | Load Balancer com TLS; NAT Gateway para encaminhamento de saída | Internet → Load Balancer em TCP/443 (HTTPS); sem entrada iniciada pela internet ao NAT Gateway | Load Balancer → Nginx em TCP/80; saída privada roteada pelo NAT e controlada pelas regras de firewall/saída |
| Sub-rede privada de aplicação | 10.0.10.0/24 | Privada de aplicação | Frontend (Nginx), Backend API, RabbitMQ, Worker e DNS Resolver/Forwarder privado (propostos) | Da subnet pública: Load Balancer → Nginx em TCP/80. Fluxos internos restritos: Nginx → Backend em TCP/8080; Backend/Worker → RabbitMQ em TCP/5672; Backend → DNS em UDP/53 | Backend → banco privado em TCP/5432; consultas DNS em UDP/53; Supabase externo aprovado em TCP/5432 via NAT e regra de saída, se utilizado |
| Sub-rede privada de dados | 10.0.20.0/24 | Privada de dados | PostgreSQL autogerenciado (arquitetura-alvo) | Somente Backend da sub-rede de aplicação em TCP/5432 | Nenhuma saída para a internet; sem rota para Internet Gateway ou NAT Gateway |

Na arquitetura proposta, Load Balancer e NAT Gateway ficam na subnet pública; Frontend, Backend, RabbitMQ, Worker e DNS Resolver/Forwarder privado ficam na subnet privada de aplicação; PostgreSQL autogerenciado fica na subnet privada de dados. Usuário/Internet e o Supabase atualmente usado são externos à VPC e, por isso, não são artificialmente alocados em subnets do projeto. O NAT Gateway encaminha conexões iniciadas pelas subnets privadas, mas não substitui regras de firewall/saída. No Compose atual, `rotavital-frontend` e `rotavital-backend` compartilham a rede padrão `<nome-do-projeto>_default` (`bridge`); as portas do host 80 e 8080 estão publicadas. Essa execução local não implementa as regras de isolamento de produção.

Regra de ouro: o banco não fica em sub-rede pública e nunca aceita conexão iniciada diretamente pela internet.

---

## 7. Legenda e convenções

### Cores

- Azul: rede pública / entrada externa
- Verde: aplicação / serviços web e backend
- Roxo: dados / banco

### Formas

- Usuário / Internet: ícone de usuário
- Load Balancer / Gateway: ícone de roteador
- Frontend: servidor web
- Backend: servidor de aplicação
- Banco: ícone de banco de dados

### Tipos de linha

- Contínua: comunicação síncrona
- Tracejada: comunicação assíncrona proposta (Backend → RabbitMQ → Worker; AMQP/TCP/5672; não configurada no Compose atual)
- Pontilhada fina: métricas/logs e observabilidade (convenção; não configurada no projeto atual)

O `.drawio` está salvo em [`DIAGRAMA_ROTAVITAL.drawio`](./DIAGRAMA_ROTAVITAL.drawio). As exportações PNG a 300% e PDF Fit Page ainda precisam ser geradas e salvas em `docs/`.

---

## Verificação cruzada

| Pergunta de verificação | Resultado | Evidência/observação |
|---|---|---|
| Todo endpoint da tabela está exposto por algum componente do diagrama? | **Sim** | Os endpoints REST são servidos pelo Backend API, representado na sub-rede privada de aplicação. |
| Toda seta do diagrama tem uma linha na tabela de protocolos? | **Sim** | As sete setas (`line1` a `line7`) correspondem às sete linhas da tabela da seção 5, incluindo AMQP/TCP/5672 para mensageria assíncrona. |
| Todo componente do diagrama está dentro de alguma sub-rede da tabela de redes? | **Sim, para os componentes implantáveis da VPC** | Load Balancer e NAT estão na sub-rede pública; Frontend, Backend, RabbitMQ, Worker e DNS privado estão na sub-rede privada de aplicação; PostgreSQL está na sub-rede privada de dados. O ícone Usuário/Internet está explicitamente fora da VPC e é um ator externo, não um componente implantado que deva ser alocado em subnet. |

### Revisão contra erros comuns do enunciado

| Erro a evitar | Revisão | Resultado |
|---|---|---|
| Usar HTTP puro na ligação que vem da internet | Navegador/Internet → Load Balancer está rotulado HTTPS/443; HTTP/1.1/80 aparece somente entre Load Balancer e Nginx na rede privada. | **Não encontrado** |
| Colocar ou expor o banco na sub-rede pública | PostgreSQL autogerenciado está na sub-rede privada de dados `10.0.20.0/24`, aceita somente TCP/5432 do Backend e não tem saída para a internet. | **Não encontrado** |
| Deixar setas sem protocolo/porta ou omitir os fluxos assíncronos | Todas as sete setas têm protocolo e porta na tabela; publicação e consumo AMQP/TCP/5672 aparecem como ligações tracejadas. | **Não encontrado** |
| Alocar incorretamente atores/serviços externos em sub-redes internas | Usuário/Internet e Supabase gerenciado estão identificados como externos à VPC; não são apresentados como workloads internos. | **Não encontrado** |
| Confundir a rede local do Compose com as sub-redes propostas para produção | O mapeamento identifica a rede `bridge` padrão atual separadamente da arquitetura proposta e registra a publicação local das portas 80 e 8080. | **Não encontrado** |

Checklist complementar:

- [x] Componentes implantáveis alocados em subnet; ator externo identificado fora da VPC.
- [x] Três verificações cruzadas respondidas afirmativamente, com a exceção externa explicitada.
- [x] Revisão contra os erros de segurança e consistência listados nesta seção registrada.
- [x] As sete seções estão organizadas na ordem exigida neste documento.
- [ ] Exportações PNG a 300% e PDF Fit Page geradas e salvas.

Conclusão da revisão: não foram encontrados os erros comuns listados acima. A montagem das sete seções e a verificação cruzada estão registradas; o **PDF final ainda não foi exportado**, portanto esse artefato continua pendente.

---

## Observação de exportação final

Para gerar as exportações, abra o arquivo [DIAGRAMA_ROTAVITAL.drawio](./DIAGRAMA_ROTAVITAL.drawio) no draw.io/app.diagrams.net e execute:

- File → Export as → PDF, selecionando `Fit Page`
- File → Export as → PNG, com zoom `300%` e fundo transparente desmarcado

Salve os arquivos exportados na pasta `docs/`. No momento, somente o `.drawio` está salvo; as exportações ainda estão pendentes.

Em seguida, reúna as seções neste documento na ordem solicitada, mantendo a capa, descrição, diagrama, endpoints, ligações, redes e legenda.
