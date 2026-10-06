-- ===================================================================
-- Rota Vital — Schema inicial (tabelas, PKs e FKs)
-- Espelha o pacote com.rotavital.dominio e o contrato docs/openapi.yaml.
-- As regras de integridade de negócio (NOT NULL, UNIQUE, CHECK, gatilhos)
-- ficam na migration seguinte: 20260924120100_constraints_integridade.sql
-- ===================================================================

-- PontoDeRede (Hospital e BancoDeSangue) — nós do grafo de distribuição.
-- Endereco é value object: vira colunas da própria tabela.
create table public.ponto_rede
(
    id          text primary key,
    tipo        text,
    nome        text,
    logradouro  text,
    latitude    double precision,
    longitude   double precision,
    criado_em   timestamptz
);

-- Conexao — aresta dirigida do grafo (a rede grava ida e volta como duas linhas).
create table public.conexao
(
    id                  bigint generated always as identity primary key,
    origem_id           text references public.ponto_rede (id) on delete cascade,
    destino_id          text references public.ponto_rede (id) on delete cascade,
    distancia_km        numeric(8, 2),
    tempo_estimado_min  numeric(8, 2)
);

-- BolsaHemocomponente — item do Estoque de um BancoDeSangue.
create table public.bolsa_hemocomponente
(
    id                  text primary key,
    banco_origem_id     text references public.ponto_rede (id) on delete restrict,
    tipo_componente     text,
    tipo_sanguineo      text,
    data_coleta         date,
    data_validade       date,
    lote_sintetico      text,
    volume_ml           numeric(6, 1),
    temperatura_celsius numeric(5, 2),
    localizacao         text,
    status              text,
    criado_em           timestamptz
);

-- RequisicaoHospitalar — pedido de um Hospital.
create table public.requisicao_hospitalar
(
    id               uuid primary key default gen_random_uuid(),
    hospital_id      text references public.ponto_rede (id) on delete restrict,
    tipo_componente  text,
    tipo_sanguineo   text,
    quantidade       integer,
    urgencia         text,
    data_solicitacao timestamptz,
    status           text
);

-- Alocacao — vínculo requisição ↔ bolsa escolhida pelo FEFO.
create table public.alocacao
(
    id             uuid primary key default gen_random_uuid(),
    requisicao_id  uuid references public.requisicao_hospitalar (id) on delete cascade,
    bolsa_id       text references public.bolsa_hemocomponente (id) on delete restrict,
    data_alocacao  timestamptz
);

-- Entrega — transporte de uma requisição do banco até o hospital.
create table public.entrega
(
    id             uuid primary key default gen_random_uuid(),
    requisicao_id  uuid references public.requisicao_hospitalar (id) on delete restrict,
    origem_id      text references public.ponto_rede (id) on delete restrict,
    destino_id     text references public.ponto_rede (id) on delete restrict,
    status         text,
    saida_em       timestamptz,
    chegada_em     timestamptz
);

-- LeituraTelemetria — GPS + °C da maleta/veículo durante a entrega.
create table public.leitura_telemetria
(
    id                  bigint generated always as identity primary key,
    entrega_id          uuid references public.entrega (id) on delete cascade,
    registrado_em       timestamptz,
    latitude            double precision,
    longitude           double precision,
    temperatura_celsius numeric(5, 2)
);

-- Índices para as FKs mais consultadas (Postgres não cria índice em FK sozinho).
create index idx_conexao_origem on public.conexao (origem_id);
create index idx_bolsa_banco on public.bolsa_hemocomponente (banco_origem_id);
create index idx_requisicao_hospital on public.requisicao_hospitalar (hospital_id);
create index idx_alocacao_requisicao on public.alocacao (requisicao_id);
create index idx_entrega_requisicao on public.entrega (requisicao_id);

-- Tabelas em "public" ficam expostas pela API REST do Supabase (chave anon).
-- Ligar RLS sem políticas bloqueia esse acesso; o backend usa o role postgres,
-- que ignora RLS. As políticas ficam na subtarefa de RLS.
alter table public.ponto_rede            enable row level security;
alter table public.conexao               enable row level security;
alter table public.bolsa_hemocomponente  enable row level security;
alter table public.requisicao_hospitalar enable row level security;
alter table public.alocacao              enable row level security;
alter table public.entrega               enable row level security;
alter table public.leitura_telemetria    enable row level security;
