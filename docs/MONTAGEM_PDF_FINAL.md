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
- Backend API → DNS Resolver: DNS/UDP/53

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

Observações:

- a conexão vinda da internet termina no gateway por HTTPS; HTTP/1.1/80 ocorre somente entre gateway e Nginx na rede privada;
- na arquitetura-alvo, o banco não é exposto diretamente à internet (no projeto atual, o Supabase é um serviço externo gerenciado);
- não há fila ou fluxo assíncrono configurado no projeto atual;
- o Compose atual publica as portas do host 80 (Frontend) e 8080 (API), sem gateway/TLS; HTTPS na borda e restrição de acesso direto à API dependem da configuração de produção.

---

## 6. Tabela de definição de redes

| Rede/Sub-rede | CIDR | Tipo | Componentes | Entrada permitida | Saída permitida |
|---|---|---|---|---|---|
| Rede principal (VPC) | 10.0.0.0/16 | Rede privada virtual | Sub-redes pública, de aplicação e de dados; DNS Resolver gerenciado e compartilhado | Regras aplicadas nas sub-redes; DNS aceita UDP/53 das origens autorizadas | Conforme regras de cada sub-rede |
| Sub-rede pública | 10.0.1.0/24 | Pública | Load Balancer com TLS; NAT Gateway para encaminhamento de saída | Internet → Load Balancer em TCP/443 (HTTPS); sem entrada iniciada pela internet ao NAT Gateway | Load Balancer → Nginx em TCP/80; DNS Resolver em UDP/53; saída privada roteada pelo NAT e controlada pelas regras de firewall/saída |
| Sub-rede privada de aplicação | 10.0.10.0/24 | Privada de aplicação | Frontend (Nginx), Backend API | Load Balancer → Nginx em TCP/80; Nginx → Backend em TCP/8080 por regra interna restrita | Backend → banco privado em TCP/5432; DNS em UDP/53; Supabase externo aprovado em TCP/5432 via NAT e regra de saída, se utilizado |
| Sub-rede privada de dados | 10.0.20.0/24 | Privada de dados | PostgreSQL autogerenciado (arquitetura-alvo) | Somente Backend da sub-rede de aplicação em TCP/5432 | Nenhuma saída para a internet; sem rota para Internet Gateway ou NAT Gateway |

O usuário/Internet é externo à VPC. O DNS Resolver é um serviço gerenciado compartilhado, não um container nem um servidor na sub-rede pública. O NAT Gateway encaminha conexões iniciadas pelas sub-redes privadas, mas não substitui regras de firewall/saída. O banco Supabase atualmente configurado é externo e gerenciado; não pertence à sub-rede de dados da VPC. Se for autogerenciado, o PostgreSQL deve ficar na sub-rede privada de dados. No Compose atual, `rotavital-frontend` e `rotavital-backend` compartilham a rede padrão `<nome-do-projeto>_default` (`bridge`); as portas do host 80 e 8080 estão publicadas. Essa execução local não implementa as regras de isolamento de produção.

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
- Tracejada: comunicação assíncrona (convenção; não configurada no projeto atual)
- Pontilhada fina: métricas/logs e observabilidade (convenção; não configurada no projeto atual)

O `.drawio` está salvo em [`DIAGRAMA_ROTAVITAL.drawio`](./DIAGRAMA_ROTAVITAL.drawio). As exportações PNG a 300% e PDF Fit Page ainda precisam ser geradas e salvas em `docs/`.

---

## Verificação cruzada

Checklist final:

- [x] Todo endpoint da tabela está exposto por algum componente do diagrama?
- [x] Toda seta do diagrama tem uma linha na tabela de protocolos?
- [x] Todo componente de workload do diagrama está alocado em uma sub-rede?
- [x] Internet está identificada como origem externa e DNS como serviço compartilhado da rede?
- [x] O Compose atual está mapeado sem confundir a rede bridge local com as sub-redes propostas?
- [x] O arquivo editável `.drawio` está salvo?
- [ ] As exportações PNG a 300% e PDF Fit Page foram geradas e salvas?

Conclusão: a compilação das sete seções está estruturada e a verificação cruzada está registrada. O PDF final ainda não foi gerado; falta exportar o diagrama do draw.io e inserir as exportações nesta compilação.

---

## Observação de exportação final

Para gerar as exportações, abra o arquivo [DIAGRAMA_ROTAVITAL.drawio](./DIAGRAMA_ROTAVITAL.drawio) no draw.io/app.diagrams.net e execute:

- File → Export as → PDF, selecionando `Fit Page`
- File → Export as → PNG, com zoom `300%` e fundo transparente desmarcado

Salve os arquivos exportados na pasta `docs/`. No momento, somente o `.drawio` está salvo; as exportações ainda estão pendentes.

Em seguida, reúna as seções neste documento na ordem solicitada, mantendo a capa, descrição, diagrama, endpoints, ligações, redes e legenda.
