# Tabela de ligações e protocolos e definição de redes/sub-redes

## 1. Inventário dos componentes considerados

Com base no projeto atual em [docker-compose.yml](./docker-compose.yml) e na configuração do banco em [backend/src/main/resources/application.properties](../backend/src/main/resources/application.properties), os componentes relevantes são:

- Navegador / Internet
- Load Balancer / Gateway (arquitetura-alvo; não configurado no Compose atual)
- NAT Gateway (arquitetura-alvo; não configurado no Compose atual)
- Frontend (React + Vite + Nginx)
- Backend API (Spring Boot)
- RabbitMQ (broker de mensagens; arquitetura proposta, não configurado no Compose atual)
- Worker assíncrono (consumidor de eventos; arquitetura proposta, não configurado no Compose atual)
- PostgreSQL / Supabase (Supabase gerenciado externo atualmente; PostgreSQL autogerenciado na sub-rede privada de dados como arquitetura-alvo)
- DNS Resolver/Forwarder privado (endpoint de rede da arquitetura proposta)

> Observação: no `docker-compose.yml` atual não há redes explícitas definidas. O Compose usa a rede padrão do Docker (`bridge`), portanto a segmentação abaixo representa a arquitetura de produção desejada, com isolamento lógico por sub-rede e controle de acesso.
> Usuário/Internet e o Supabase gerenciado atual são pares externos, não workloads implantados na VPC do projeto; por isso não recebem CIDR de subnet. Todos os componentes implantáveis da arquitetura proposta são alocados na tabela da seção 3.

---

## 2. Tabela de ligações e protocolos

Descrição: identifica o protocolo e a porta de cada ligação entre componentes, conforme o enunciado.

| # | Origem | Destino | Protocolo | Porta |
|---:|---|---|---|---:|
| 1 | Navegador / Internet | Load Balancer / Gateway | HTTPS (TLS 1.3) | 443 |
| 2 | Load Balancer / Gateway | Frontend (Nginx) | HTTP/1.1 | 80 |
| 3 | Frontend (Nginx) | Backend API | HTTP/1.1 | 8080 |
| 4 | Backend API | PostgreSQL / Supabase | TCP (PostgreSQL/JDBC, TLS) | 5432 |
| 5 | Backend API | DNS Resolver | DNS sobre UDP | 53 |
| 6 | Backend API (publicador) | RabbitMQ (fila/eventos) | AMQP 0-9-1 sobre TCP | 5672 |
| 7 | RabbitMQ (fila/eventos) | Worker assíncrono (consumidor) | AMQP 0-9-1 sobre TCP | 5672 |

### Observações importantes

- O HTTPS termina no Load Balancer / Gateway. A comunicação em HTTP/1.1 na porta 80 entre o gateway e o Nginx ocorre somente na rede privada.
- O Nginx encaminha as chamadas `/api/` ao Backend por HTTP/1.1 na porta 8080.
- O Backend acessa o Supabase por PostgreSQL/JDBC sobre TCP na porta 5432; a URL configurada exige TLS (`sslmode=require`).
- O Backend consulta DNS para resolver o hostname do Supabase por UDP na porta 53.
- As linhas tracejadas do diagrama representam o fluxo assíncrono proposto: o Backend publica mensagens/eventos no RabbitMQ e um Worker os consome desacoplado da requisição. AMQP 0-9-1 usa TCP/5672.
- RabbitMQ e Worker são componentes da arquitetura proposta para identificar a comunicação assíncrona exigida; não estão implementados nem configurados no código ou no Compose atual.
- Na arquitetura-alvo, o banco fica em rede privada e só é acessível pela aplicação. No projeto atual, o Supabase é um serviço externo gerenciado.
- O Load Balancer / Gateway com TLS representa a arquitetura-alvo. O Compose atual não configura esse componente nem TLS e publica as portas do host 80 (Frontend) e 8080 (API). Para cumprir a regra HTTPS em produção, deve-se provisionar TLS na borda e restringir o acesso direto à API.

---

## 3. Definição de redes e sub-redes

### 3.1 Rede principal

| Rede/Sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| Rede principal (VPC) | 10.0.0.0/16 | Rede privada virtual | Sub-redes pública, de aplicação e de dados | Regras aplicadas nas sub-redes | Conforme regras de cada sub-rede |

### 3.2 Sub-redes propostas

| Rede/Sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| Sub-rede pública | 10.0.1.0/24 | Pública | Load Balancer com TLS; NAT Gateway para encaminhamento de saída | Internet → Load Balancer em TCP/443 (HTTPS). Nenhuma entrada iniciada pela internet ao NAT Gateway | Load Balancer → Nginx na sub-rede de aplicação em TCP/80; tráfego de saída das sub-redes privadas roteado pelo NAT Gateway e controlado pelas regras de firewall/saída |
| Sub-rede privada de aplicação | 10.0.10.0/24 | Privada de aplicação | Frontend (Nginx), Backend API, RabbitMQ, Worker e DNS Resolver/Forwarder privado (propostos) | Da subnet pública: Load Balancer → Nginx em TCP/80. Fluxos internos restritos: Nginx → API em TCP/8080; Backend/Worker → RabbitMQ em TCP/5672; Backend → DNS Resolver em UDP/53 | Backend → PostgreSQL na subnet de dados em TCP/5432; consultas DNS em UDP/53; se o Supabase gerenciado externo for usado, saída somente ao endpoint aprovado em TCP/5432, roteada via NAT Gateway e controlada por firewall/regra de saída |
| Sub-rede privada de dados | 10.0.20.0/24 | Privada de dados | PostgreSQL autogerenciado (arquitetura-alvo) | Somente Backend API da sub-rede de aplicação em TCP/5432 | Nenhuma saída para a internet; sem rota para Internet Gateway ou NAT Gateway |

#### Alocação de todos os componentes do inventário

| Componente | Alocação na arquitetura proposta | Situação atual |
|---|---|---|
| Load Balancer / Gateway com TLS | Sub-rede pública `10.0.1.0/24` | Não configurado no Compose |
| NAT Gateway | Sub-rede pública `10.0.1.0/24` | Não configurado no Compose |
| Frontend (Nginx) | Sub-rede privada de aplicação `10.0.10.0/24` | Container `frontend` na rede Compose padrão |
| Backend API | Sub-rede privada de aplicação `10.0.10.0/24` | Container `backend` na rede Compose padrão |
| RabbitMQ | Sub-rede privada de aplicação `10.0.10.0/24` | Componente proposto; não configurado no Compose |
| Worker assíncrono | Sub-rede privada de aplicação `10.0.10.0/24` | Componente proposto; não configurado no Compose |
| DNS Resolver/Forwarder privado | Sub-rede privada de aplicação `10.0.10.0/24` | Serviço de rede da arquitetura proposta; não é container do Compose |
| PostgreSQL autogerenciado | Sub-rede privada de dados `10.0.20.0/24` | Alternativa de arquitetura; não está no Compose atual |
| Usuário / Internet | Externo à VPC; não é componente implantado na rede do projeto | Origem externa da conexão HTTPS |
| Supabase gerenciado atual | Externo à VPC; serviço gerenciado fora das sub-redes do projeto | Banco usado atualmente; acesso de saída controlado pela aplicação |

Assim, todos os componentes implantáveis pertencentes à VPC têm subnet definida. Usuário/Internet e Supabase são explicitamente tratados como pares externos, não alocados artificialmente em uma subnet do projeto. O DNS Resolver/Forwarder representado no desenho é um endpoint privado implantado na subnet de aplicação; não é um resolver público nem um container do Compose.

### 3.3 Regras de acesso e isolamento

- Internet → Sub-rede pública: permitir somente TCP/443 até o Load Balancer. Não publicar o banco nem a API diretamente.
- Sub-rede pública → Sub-rede privada de aplicação: permitir TCP/80 do Load Balancer apenas ao Nginx; não permitir entrada direta ao Backend pela internet.
- Comunicação interna na sub-rede de aplicação: permitir TCP/8080 do Nginx ao Backend por regra de segurança restrita ao serviço/origem.
- Mensageria interna proposta: permitir TCP/5672 somente do Backend e do Worker ao RabbitMQ; o Worker consome a fila por conexão AMQP estabelecida ao broker. Não expor a porta do broker à internet.
- Sub-rede privada de aplicação → sub-rede privada de dados: permitir TCP/5432 somente do Backend ao PostgreSQL.
- DNS interno: permitir UDP/53 do Backend ao DNS Resolver/Forwarder privado na sub-rede de aplicação; não aceitar consultas originadas da internet.
- Saída da aplicação ao Supabase gerenciado: se mantido como banco externo, permitir no firewall/regra de saída somente o endpoint aprovado em TCP/5432; encaminhar pela rota do NAT Gateway e aplicar TLS.
- O NAT Gateway faz tradução/encaminhamento de endereços para conexões iniciadas pelas sub-redes privadas; não substitui firewall nem regras de saída.
- Sub-rede privada de dados: sem rota de saída para Internet Gateway ou NAT Gateway.
- Banco de dados: nunca fica em sub-rede pública e nunca aceita conexão iniciada diretamente pela internet.

---

## 4. Mapeamento para Docker Compose

O [docker-compose.yml](./docker-compose.yml) atual não declara `networks`; portanto, o Compose conecta `rotavital-frontend` e `rotavital-backend` à rede padrão `<nome-do-projeto>_default`, do driver `bridge`.

| Serviço/container atual | Rede Compose atual | Publicação de porta atual | Alocação na arquitetura proposta |
|---|---|---|---|
| `frontend` / `rotavital-frontend` | `<nome-do-projeto>_default` (`bridge`) | Host TCP/80 → container TCP/80 | Sub-rede privada de aplicação; recebe tráfego do Load Balancer em TCP/80 |
| `backend` / `rotavital-backend` | `<nome-do-projeto>_default` (`bridge`) | Host TCP/8080 → container TCP/8080 | Sub-rede privada de aplicação; recebe TCP/8080 do Nginx e acessa o banco por TCP/5432 |
| RabbitMQ / Worker assíncrono | Não configurados no Compose atual | Não publicados | Componentes propostos na sub-rede privada de aplicação; AMQP/TCP/5672 somente em tráfego interno |
| PostgreSQL / Supabase | Fora do Compose; Supabase gerenciado externo | Não publicado pelo Compose | Fora das sub-redes da VPC do projeto na configuração atual; arquitetura alternativa autogerenciada na sub-rede privada de dados |
| Load Balancer / NAT Gateway | Não configurados no Compose | Não aplicável | Sub-rede pública, provisionados na infraestrutura de produção |
| DNS Resolver/Forwarder privado | Não configurado no Compose | Não publicado | Endpoint privado proposto na sub-rede de aplicação; recebe consultas internas UDP/53 |

No Compose atual, as portas 80 e 8080 são publicadas no host e não há configuração de TLS, Load Balancer ou firewall de entrada neste arquivo. Isso serve para execução local e não implementa o isolamento da arquitetura proposta. Em produção, a API não deve ficar publicamente exposta na porta 8080.

---

## 5. Conclusão

A arquitetura correta para o RotaVital, conforme os requisitos do enunciado, é:

- borda da internet em HTTPS na porta 443;
- Frontend e Backend em sub-rede privada de aplicação, com entrada do Load Balancer apenas ao Nginx em TCP/80;
- banco autogerenciado em sub-rede privada de dados, acessível somente pelo Backend em TCP/5432 e sem saída para a internet;
- se mantido o Supabase externo, conexão iniciada pelo Backend via NAT Gateway, restrita por firewall/regra de saída ao endpoint aprovado em TCP/5432 e protegida por TLS;
- DNS resolvido por endpoint privado alocado na sub-rede de aplicação, sem expor serviço DNS na sub-rede pública;
- Compose local documentado separadamente da topologia proposta para produção.

Essa modelagem atende aos critérios solicitados de:

- rede principal com CIDR;
- sub-redes com classificação pública/privada;
- regras de entrada e saída com origem e porta;
- componentes alocados corretamente;
- banco isolado do acesso externo.
