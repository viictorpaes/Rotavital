# Diagrama do RotaVital em draw.io

## Objetivo

Este documento descreve o desenho da arquitetura do RotaVital em formato `.drawio`, com separação por redes e sub-redes, protocolo/porta em todas as setas, e legenda obrigatória.

## Arquivo principal

- [DIAGRAMA_ROTAVITAL.drawio](./DIAGRAMA_ROTAVITAL.drawio)

## Estrutura do desenho

- Rede pública: 10.0.1.0/24
- Sub-rede de aplicação: 10.0.10.0/24
- Sub-rede de dados: 10.0.20.0/24
- Rede principal: 10.0.0.0/16

## Componentes representados

- Usuário / Internet
- Load Balancer / Gateway
- DNS
- Frontend
- Backend API
- PostgreSQL / Supabase
- Observabilidade (métricas e logs)

## Linhas e rótulos

- Síncrono: linha contínua com rótulo `PROTOCOLO/porta`
- Assíncrono: linha tracejada
- Observabilidade: linha pontilhada fina

## Fluxos principais

- Usuário → Load Balancer: HTTPS/443
- Load Balancer → Frontend: HTTPS/443
- Frontend → Backend: HTTP/1.1/8080
- Backend → PostgreSQL: PostgreSQL/TCP/5432
- Gateway → DNS: DNS/UDP/53
- Backend → Observabilidade: Métricas/logs

## Regras atendidas

- banco de dados isolado fora da rede pública;
- toda seta rotulada com protocolo e porta;
- legenda presente;
- desenho organizado por redes e sub-redes;
- componentes do inventário dentro das sub-redes corretas.

> O arquivo `.drawio` pode ser aberto diretamente no draw.io ou em app.diagrams.net para edição e exportação em PNG/PDF.
