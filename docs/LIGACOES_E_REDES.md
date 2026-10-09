# Tabela de ligações e protocolos e definição de redes/sub-redes

> Versão em texto das seções 5 e 6 do documento entregue na atividade de RSD
> ([`pdf/Arquitetura-RotaVital-Victor-Paes.pdf`](../pdf/Arquitetura-RotaVital-Victor-Paes.pdf)). O desenho correspondente está em
> [`Arquitetura-RotaVital-Victor-Paes.drawio`](Arquitetura-RotaVital-Victor-Paes.drawio), e cada seta `[1]`–`[5]` do
> diagrama é uma linha da tabela da seção 2.

## 1. Componentes considerados

Só entra aqui o que existe no código, no [`docker-compose.yml`](../docker-compose.yml) e na configuração do
backend ([`application.properties`](../backend/src/main/resources/application.properties)). O levantamento
completo está em [`INVENTARIO_COMPONENTES.md`](INVENTARIO_COMPONENTES.md).

| Componente | Onde mora | Situação |
|---|---|---|
| Navegador (executa a SPA React) | Internet (fora da VPC) | Ativo |
| Nginx — container `frontend` (serve a SPA e faz proxy reverso de `/api/*`) | `sub-publica` | Ativo |
| Backend API — container `backend` (Spring Boot 3.3, Java 21, porta 8080) | `sub-app` | Ativo |
| PostgreSQL (Supabase gerenciado) | `sub-dados` | Ativo (somente leitura) |
| OSRM — `router.project-osrm.org` (terceiro) | Internet (fora da VPC) | Ativo |
| OpenStreetMap — `tile.openstreetmap.org` (terceiro) | Internet (fora da VPC) | Ativo |

**O que não existe no projeto** (e por isso não aparece no diagrama nem nas tabelas): fila ou mensageria,
cache, balanceador de carga separado (o Nginx cumpre esse papel), NAT Gateway, servidor de e-mail, DNS
privado e ferramenta de observabilidade (Prometheus, Grafana etc.). A observação do sistema é feita pelo
próprio backend: o `HttpLoggingFilter` guarda as últimas 60 requisições HTTP em memória e o
`GET /api/v1/diagnostico` as expõe, junto com uptime e latência do banco, no painel de Administração.

---

## 2. Tabela de ligações e protocolos

Formato do rótulo de cada seta no diagrama: `[nº] PROTOCOLO / porta`.

| # | Origem | Destino | Protocolo | Porta | Situação |
|---:|---|---|---|---:|---|
| 1 | Navegador | Nginx (frontend) | HTTPS (HTTP/1.1 ou HTTP/2 sobre TLS 1.3) | 443 | Ativa |
| 2 | Navegador | OSRM (terceiro) | HTTPS | 443 | Ativa |
| 3 | Navegador | OpenStreetMap tiles (terceiro) | HTTPS | 443 | Ativa |
| 4 | Nginx (frontend) | Backend API | HTTP/1.1 (proxy reverso de `/api/*`) | 8080 | Ativa — estoque, pontos e diagnóstico |
| 5 | Backend API | PostgreSQL (Supabase) | Protocolo PostgreSQL sobre TCP (driver JDBC, com TLS: `sslmode=require`) | 5432 | Ativa — leitura via JPA |

### Observações

- **Borda sempre em HTTPS:** tudo que vem da internet chega ao Nginx em HTTPS/443. O TLS termina no Nginx;
  dali para dentro, na rede privada, o tráfego segue em HTTP/1.1 na porta 8080.
- **Banco nunca exposto:** só o Backend API alcança a porta 5432; nenhuma seta sai da internet para o banco.
- **Sem comunicação assíncrona:** não há fila nem eventos no projeto, por isso não há linha tracejada.
- **Terceiros chamados pelo navegador:** OSRM ([`roteirizacao.ts`](../frontend/src/lib/roteirizacao.ts),
  timeout de 8 s com fallback) e tiles ([`MapaRede.tsx`](../frontend/src/components/rede/MapaRede.tsx)) são
  acessados direto do navegador, não pelo servidor. Assim nenhum componente da VPC precisa sair para a
  internet, e não é necessário NAT Gateway.
- **Ambiente local × produção:** no `docker-compose` de desenvolvimento o Nginx atende em HTTP na porta 80 do
  `localhost` (`listen 80` em [`frontend/nginx.conf`](../frontend/nginx.conf)). No ambiente projetado ele
  escuta apenas na 443, com o certificado TLS do domínio; essa configuração de produção ainda não está no
  repositório.

---

## 3. Definição de redes e sub-redes

### 3.1 Tabela de redes

| Rede / sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| VPC RotaVital | `10.0.0.0/16` | Rede principal | Todas as sub-redes abaixo | — | — |
| `sub-publica` | `10.0.1.0/24` | Pública | Nginx (container `frontend`) | `0.0.0.0/0` → TCP 443 | `10.0.10.0/24` → TCP 8080 |
| `sub-app` | `10.0.10.0/24` | Privada de aplicação | Backend API | `10.0.1.0/24` → TCP 8080 | `10.0.20.0/24` → TCP 5432 |
| `sub-dados` | `10.0.20.0/24` | Privada de dados | PostgreSQL (Supabase) | `10.0.10.0/24` → TCP 5432 | Nenhuma (sem internet) |

### 3.2 Regras de acesso (firewall / security groups)

| Regra | Origem | Destino | Porta | Ação | Motivo |
|---|---|---|---|---|---|
| R1 | Internet (`0.0.0.0/0`) | `sub-publica` (Nginx) | TCP 443 | Permitir | Acesso HTTPS dos usuários |
| R2 | `sub-publica` | `sub-app` (Backend) | TCP 8080 | Permitir | Nginx encaminha `/api/*` ao backend |
| R3 | `sub-app` | `sub-dados` (PostgreSQL) | TCP 5432 | Permitir | Backend acessa o banco |
| R4 | Internet | `sub-app` e `sub-dados` | Qualquer | Negar | Nada da internet chega direto à aplicação ou ao banco |
| R5 | `sub-publica` | `sub-dados` | Qualquer | Negar | O gateway não fala com o banco |
| R6 | `sub-dados` | Internet | Qualquer | Negar | Banco sem saída para a internet |

Padrão: tudo que não está listado é negado. As regras são *stateful* (a resposta de uma conexão permitida
volta automaticamente).

**Regra de ouro atendida:** o banco está na sub-rede privada de dados, só é acessível a partir da sub-rede de
aplicação na porta 5432 e não tem entrada nem saída para a internet.

**Sobre o Supabase:** é um PostgreSQL gerenciado. No ambiente projetado ele é tratado como a sub-rede de
dados, sem endereço público e acessível apenas pela sub-rede de aplicação por conexão privada. No
desenvolvimento atual o backend conecta ao pooler do Supabase pela internet com TLS, o que é aceitável só
para o MVP.

---

## 4. Correspondência com o Docker Compose atual

O [`docker-compose.yml`](../docker-compose.yml) não declara `networks`, então os dois containers ficam na rede
padrão do projeto (`<nome-do-projeto>_default`, driver `bridge`).

| Sub-rede projetada | Container (`docker-compose.yml`) | Rede Docker hoje | Porta no host |
|---|---|---|---|
| `sub-publica` | `rotavital-frontend` (nginx:alpine) | rede padrão do projeto (`bridge`) | `80:80` |
| `sub-app` | `rotavital-backend` (Java 21) | rede padrão do projeto (`bridge`) | `8080:8080` |
| `sub-dados` | — (Supabase gerenciado, fora do compose) | — | — |

O compose de desenvolvimento não implementa o isolamento: os dois containers estão na mesma rede `bridge` e a
8080 do backend é publicada no host para facilitar o desenvolvimento e os testes com Postman/Insomnia. No
ambiente projetado a 8080 só é alcançável a partir da `sub-publica` (regra R2).

---

## 5. Conclusão

A arquitetura projetada para o RotaVital atende aos critérios do enunciado:

- borda da internet só em HTTPS/443, terminando no Nginx;
- cada componente da VPC em uma sub-rede: Nginx na pública, Backend na privada de aplicação e PostgreSQL na
  privada de dados;
- regras de entrada e saída com origem e porta (R1–R6), com tudo o mais negado;
- banco isolado, sem entrada nem saída para a internet;
- toda seta do diagrama (`[1]`–`[5]`) com protocolo e porta e uma linha correspondente na tabela da seção 2;
- Compose local documentado separadamente da topologia de produção.
