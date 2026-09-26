# Tabela de ligações e protocolos e definição de redes/sub-redes

## 1. Inventário dos componentes considerados

Com base no projeto atual em [docker-compose.yml](./docker-compose.yml) e na configuração do banco em [backend/src/main/resources/application.properties](../backend/src/main/resources/application.properties), os componentes relevantes são:

- Navegador / Internet
- Load Balancer / Gateway (arquitetura-alvo; não configurado no Compose atual)
- NAT Gateway (arquitetura-alvo; não configurado no Compose atual)
- Frontend (React + Vite + Nginx)
- Backend API (Spring Boot)
- PostgreSQL / Supabase (Supabase gerenciado externo atualmente; PostgreSQL autogerenciado na sub-rede privada de dados como arquitetura-alvo)
- DNS Resolver gerenciado e compartilhado pela rede principal

> Observação: no `docker-compose.yml` atual não há redes explícitas definidas. O Compose usa a rede padrão do Docker (`bridge`), portanto a segmentação abaixo representa a arquitetura de produção desejada, com isolamento lógico por sub-rede e controle de acesso.

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

### Observações importantes

- O HTTPS termina no Load Balancer / Gateway. A comunicação em HTTP/1.1 na porta 80 entre o gateway e o Nginx ocorre somente na rede privada.
- O Nginx encaminha as chamadas `/api/` ao Backend por HTTP/1.1 na porta 8080.
- O Backend acessa o Supabase por PostgreSQL/JDBC sobre TCP na porta 5432; a URL configurada exige TLS (`sslmode=require`).
- O Backend consulta DNS para resolver o hostname do Supabase por UDP na porta 53.
- Na arquitetura-alvo, o banco fica em rede privada e só é acessível pela aplicação. No projeto atual, o Supabase é um serviço externo gerenciado.
- Não há fila, broker ou outro fluxo assíncrono configurado no projeto atual. Portanto, o diagrama não inventa ligações assíncronas.
- O Load Balancer / Gateway com TLS representa a arquitetura-alvo. O Compose atual não configura esse componente nem TLS e publica as portas do host 80 (Frontend) e 8080 (API). Para cumprir a regra HTTPS em produção, deve-se provisionar TLS na borda e restringir o acesso direto à API.

---

## 3. Definição de redes e sub-redes

### 3.1 Rede principal

| Rede/Sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| Rede principal (VPC) | 10.0.0.0/16 | Rede privada virtual | Sub-redes pública, de aplicação e de dados; DNS Resolver gerenciado e compartilhado | Regras aplicadas nas sub-redes; DNS aceita UDP/53 das origens autorizadas | Conforme regras de cada sub-rede |

### 3.2 Sub-redes propostas

| Rede/Sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| Sub-rede pública | 10.0.1.0/24 | Pública | Load Balancer com TLS; NAT Gateway para encaminhamento de saída | Internet → Load Balancer em TCP/443 (HTTPS). Nenhuma entrada iniciada pela internet ao NAT Gateway | Load Balancer → Nginx na sub-rede de aplicação em TCP/80; DNS Resolver em UDP/53; tráfego de saída das sub-redes privadas roteado pelo NAT Gateway e controlado pelas regras de firewall/saída |
| Sub-rede privada de aplicação | 10.0.10.0/24 | Privada de aplicação | Frontend (Nginx), Backend API | Load Balancer da sub-rede pública → Nginx em TCP/80; comunicação interna restrita Nginx → API em TCP/8080 | Backend → PostgreSQL na sub-rede de dados em TCP/5432; consultas ao DNS Resolver em UDP/53; se o Supabase gerenciado externo for usado, saída somente ao endpoint aprovado em TCP/5432, roteada via NAT Gateway e controlada por firewall/regra de saída |
| Sub-rede privada de dados | 10.0.20.0/24 | Privada de dados | PostgreSQL autogerenciado (arquitetura-alvo) | Somente Backend API da sub-rede de aplicação em TCP/5432 | Nenhuma saída para a internet; sem rota para Internet Gateway ou NAT Gateway |

O usuário/Internet é externo à VPC. O DNS Resolver é um serviço gerenciado compartilhado pela rede principal, não um container nem uma máquina em sub-rede pública. O PostgreSQL autogerenciado pertence à sub-rede privada de dados; quando o projeto usa o Supabase gerenciado atual, o banco fica fora da VPC do projeto e é alcançado pela saída controlada da aplicação, não deve ser representado como um recurso implantado na sub-rede de dados.

### 3.3 Regras de acesso e isolamento

- Internet → Sub-rede pública: permitir somente TCP/443 até o Load Balancer. Não publicar o banco nem a API diretamente.
- Sub-rede pública → Sub-rede privada de aplicação: permitir TCP/80 do Load Balancer apenas ao Nginx; não permitir entrada direta ao Backend pela internet.
- Comunicação interna na sub-rede de aplicação: permitir TCP/8080 do Nginx ao Backend por regra de segurança restrita ao serviço/origem.
- Sub-rede privada de aplicação → sub-rede privada de dados: permitir TCP/5432 somente do Backend ao PostgreSQL.
- Sub-rede privada de aplicação → DNS Resolver gerenciado: permitir UDP/53 para resolução de nomes.
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
| PostgreSQL / Supabase | Fora do Compose; Supabase gerenciado externo | Não publicado pelo Compose | Fora das sub-redes da VPC do projeto na configuração atual; arquitetura alternativa autogerenciada na sub-rede privada de dados |
| Load Balancer / NAT Gateway | Não configurados no Compose | Não aplicável | Sub-rede pública, provisionados na infraestrutura de produção |
| DNS Resolver | Serviço da rede/ambiente, não é container do Compose | Não aplicável | Serviço gerenciado compartilhado pela rede principal |

No Compose atual, as portas 80 e 8080 são publicadas no host e não há configuração de TLS, Load Balancer ou firewall de entrada neste arquivo. Isso serve para execução local e não implementa o isolamento da arquitetura proposta. Em produção, a API não deve ficar publicamente exposta na porta 8080.

---

## 5. Conclusão

A arquitetura correta para o RotaVital, conforme os requisitos do enunciado, é:

- borda da internet em HTTPS na porta 443;
- Frontend e Backend em sub-rede privada de aplicação, com entrada do Load Balancer apenas ao Nginx em TCP/80;
- banco autogerenciado em sub-rede privada de dados, acessível somente pelo Backend em TCP/5432 e sem saída para a internet;
- se mantido o Supabase externo, conexão iniciada pelo Backend via NAT Gateway, restrita por firewall/regra de saída ao endpoint aprovado em TCP/5432 e protegida por TLS;
- DNS resolvido por serviço gerenciado compartilhado, sem expor serviço DNS de workload na sub-rede pública;
- Compose local documentado separadamente da topologia proposta para produção.

Essa modelagem atende aos critérios solicitados de:

- rede principal com CIDR;
- sub-redes com classificação pública/privada;
- regras de entrada e saída com origem e porta;
- componentes alocados corretamente;
- banco isolado do acesso externo.
