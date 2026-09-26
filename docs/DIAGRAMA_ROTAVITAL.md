# Diagrama do RotaVital em draw.io

## Objetivo

Este documento descreve o desenho da arquitetura do RotaVital em formato `.drawio`, com separação por redes e sub-redes, protocolo/porta em todas as setas, e legenda obrigatória.

## Arquivo principal

- [DIAGRAMA_ROTAVITAL.drawio](./DIAGRAMA_ROTAVITAL.drawio)

## Estrutura do desenho

- Rede pública: 10.0.1.0/24
- Sub-rede privada de aplicação: 10.0.10.0/24
- Sub-rede privada de dados: 10.0.20.0/24
- Rede principal: 10.0.0.0/16

## Componentes representados

- Usuário / Internet
- Load Balancer / Gateway
- NAT Gateway para saída controlada
- DNS Resolver gerenciado e compartilhado pela rede principal
- Frontend
- Backend API
- PostgreSQL autogerenciado na sub-rede privada de dados (arquitetura-alvo)

## Linhas e rótulos

- Síncrono: linha contínua com rótulo `PROTOCOLO/porta`
- Assíncrono: linha tracejada; a legenda mostra a convenção, mas não há fila/evento configurado no projeto atual
- Observabilidade: linha pontilhada fina; a legenda mostra a convenção, mas não há coletor de métricas/logs configurado no projeto atual

## Fluxos principais

- Usuário → Load Balancer: HTTPS/443
- Load Balancer → Frontend (Nginx): HTTP/1.1/80 (rede privada)
- Frontend → Backend: HTTP/1.1/8080
- Backend → PostgreSQL: TCP (PostgreSQL)/5432
- Backend → DNS: DNS/UDP/53 (resolução do hostname do Supabase)

> O fluxo para PostgreSQL no desenho representa o banco autogerenciado da arquitetura-alvo. O projeto atual usa Supabase gerenciado externo, acessado pelo Backend por TCP/5432 com TLS.

## Regras atendidas

- banco de dados isolado fora da rede pública;
- todas as setas rotuladas com protocolo e porta;
- legenda com cores, formas e amostras dos três tipos de linha;
- desenho organizado por redes e sub-redes;
- Internet identificada como externa à VPC;
- componentes de workload alocados nas sub-redes corretas; DNS Resolver indicado como serviço gerenciado compartilhado sem alocação em sub-rede.

> O Load Balancer com TLS, o NAT Gateway e as sub-redes são parte da arquitetura-alvo; o Docker Compose atual não os configura. O Compose publica o Frontend em HTTP/80 e a API em 8080 na máquina hospedeira. O Supabase atual é gerenciado externamente, não está alocado na sub-rede privada de dados do projeto.

## Arquivos e exportação

- Arquivo editável `.drawio`: salvo em `docs/DIAGRAMA_ROTAVITAL.drawio`.
- Exportações PNG a 300% e PDF Fit Page: ainda pendentes; não há esses arquivos exportados no projeto.

Para gerar as exportações, abrir o `.drawio` no draw.io e usar `File → Export as → PDF` com `Fit Page` e `File → Export as → PNG` com zoom `300%` e fundo transparente desmarcado. Salvar os dois arquivos nesta pasta `docs/`.
