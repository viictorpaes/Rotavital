# Tabela de ligações e protocolos e definição de redes/sub-redes

## 1. Inventário dos componentes considerados

Com base no projeto atual em [docker-compose.yml](./docker-compose.yml) e na configuração do banco em [backend/src/main/resources/application.properties](../backend/src/main/resources/application.properties), os componentes relevantes são:

- Navegador / Internet
- Frontend (React + Vite + Nginx)
- Backend API (Spring Boot)
- PostgreSQL / Supabase
- DNS

> Observação: no `docker-compose.yml` atual não há redes explícitas definidas. O Compose usa a rede padrão do Docker (`bridge`), portanto a segmentação abaixo representa a arquitetura de produção desejada, com isolamento lógico por sub-rede e controle de acesso.

---

## 2. Tabela de ligações e protocolos

Descrição: identifica o protocolo e a porta de cada ligação entre componentes, conforme o enunciado.

| # | Origem | Destino | Protocolo | Porta |
|---:|---|---|---|---:|
| 1 | Navegador / Internet | Frontend | HTTPS (TLS 1.3) | 443 |
| 2 | Frontend | Backend API | HTTP/1.1 | 8080 |
| 3 | Backend API | PostgreSQL / Supabase | PostgreSQL sobre TCP (JDBC) | 5432 |
| 4 | Qualquer host | DNS | DNS sobre UDP | 53 |

### Observações importantes

- A borda da aplicação deve usar HTTPS, nunca HTTP puro, conforme regra de segurança do enunciado.
- O Frontend comunica-se com a API por HTTP/1.1 na porta 8080.
- O Backend acessa o banco de dados em PostgreSQL na porta 5432.
- O banco não é exposto diretamente na internet; ele fica em rede privada e acessível apenas pela aplicação.
- Não há fila assíncrona no stack atual do projeto (sem Redis, RabbitMQ, Prometheus ou SMTP configurados no compose), então não há ligação assíncrona implementada no diagrama atual.

---

## 3. Definição de redes e sub-redes

### 3.1 Rede principal

| Rede/Sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| Rede principal | 10.0.0.0/16 | Principal | Sistema inteiro | -- | -- |

### 3.2 Sub-redes propostas

| Rede/Sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| Sub-rede pública | 10.0.1.0/24 | Pública | Load balancer / gateway de entrada | Internet → 443 (HTTPS) | Para sub-rede de aplicação em 80/443; resolução DNS em 53 |
| Sub-rede de aplicação | 10.0.10.0/24 | Privada de aplicação | Frontend, Backend API | Só da sub-rede pública; Frontend em 80/443 e Backend em 8080 internamente | Para a sub-rede de dados em 5432; DNS em 53 |
| Sub-rede de dados | 10.0.20.0/24 | Privada de dados | PostgreSQL / Supabase | Só da sub-rede de aplicação em 5432 | Nenhuma saída para a internet |

### 3.3 Regras de acesso e isolamento

- Internet → Sub-rede pública:
  - HTTPS: 443
  - DNS: 53 (quando necessário para resolução externa)

- Sub-rede pública → Sub-rede de aplicação:
  - Frontend: 80/443
  - Backend: 8080 (acesso interno, não exposto diretamente)

- Sub-rede de aplicação → Sub-rede de dados:
  - PostgreSQL: 5432

- Sub-rede de dados → Internet:
  - Bloqueado por política de segurança.

- Banco de dados:
  - Nunca fica em sub-rede pública.
  - Nunca é acessível diretamente da internet.

---

## 4. Mapeamento para Docker Compose

O projeto atual não define redes personalizadas no [docker-compose.yml](./docker-compose.yml). O Compose usa a rede padrão do Docker, o que significa que os containers estão na mesma rede bridge por padrão.

### Estrutura lógica equivalente do Compose

| Container | Rede atual | Rede lógica sugerida |
|---|---|---|
| `rotavital-frontend` | rede padrão do Docker | sub-rede de aplicação |
| `rotavital-backend` | rede padrão do Docker | sub-rede de aplicação |
| Banco PostgreSQL / Supabase | externo | sub-rede de dados |

### Exemplo de isolamento em Docker Compose (arquitetura desejada)

```yaml
services:
  frontend:
    build: ./frontend
    networks:
      - app_net

  backend:
    build: ./backend
    networks:
      - app_net
      - db_net

networks:
  app_net:
    driver: bridge
    ipam:
      config:
        - subnet: 10.0.10.0/24

  db_net:
    driver: bridge
    ipam:
      config:
        - subnet: 10.0.20.0/24
```

> A arquitetura acima representa a correta separação entre aplicação e dados, mesmo que hoje o projeto rode em localhost com a configuração padrão do Docker.

---

## 5. Conclusão

A arquitetura correta para o RotaVital, conforme os requisitos do enunciado, é:

- borda da internet em HTTPS na porta 443;
- aplicação em sub-rede privada;
- banco em sub-rede privada de dados;
- sem exposição direta do banco para a internet;
- com controle rígido de entrada e saída entre as sub-redes.

Essa modelagem atende aos critérios solicitados de:

- rede principal com CIDR;
- sub-redes com classificação pública/privada;
- regras de entrada e saída com origem e porta;
- componentes alocados corretamente;
- banco isolado do acesso externo.
