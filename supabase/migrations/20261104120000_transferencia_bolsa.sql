-- ===================================================================
-- Rota Vital — Rastreabilidade das transferências de bolsas (PI3-236).
--   * bolsa_hemocomponente.hospital_atual_id: o hospital onde a bolsa está;
--   * transferencia_bolsa: de onde cada bolsa saiu, para onde foi, quem
--     despachou, quem recebeu e quando.
--
-- A instituição de onde a bolsa veio já é banco_origem_id. As tabelas que
-- existem não servem para a transferência: remessa só liga hemocentros
-- (HU-05) e entrega pertence a uma requisição, com várias bolsas.
-- ===================================================================

-- -------------------------------------------------------------------
-- bolsa_hemocomponente
-- -------------------------------------------------------------------
alter table public.bolsa_hemocomponente
    -- Nulo enquanto a bolsa está no hemocentro de origem ou em trânsito.
    add column hospital_atual_id   text,
    -- Coluna fixa usada só pela FK composta, como banco_origem_tipo.
    add column hospital_atual_tipo text not null default 'HOSPITAL',
    add constraint ck_bolsa_hospital_atual_tipo check (hospital_atual_tipo = 'HOSPITAL'),
    add constraint fk_bolsa_hospital_atual_e_hospital
        foreign key (hospital_atual_id, hospital_atual_tipo)
        references public.ponto_rede (id, tipo) on delete restrict;

create index idx_bolsa_hospital_atual on public.bolsa_hemocomponente (hospital_atual_id);

-- -------------------------------------------------------------------
-- transferencia_bolsa — uma bolsa saindo do hemocentro para um hospital
-- -------------------------------------------------------------------
create table public.transferencia_bolsa
(
    id                  uuid primary key default gen_random_uuid(),
    bolsa_id            text not null,
    origem_id           text not null,
    origem_tipo         text not null default 'BANCO_DE_SANGUE',
    destino_id          text not null,
    destino_tipo        text not null default 'HOSPITAL',
    status              text not null default 'EM_TRANSITO',
    distancia_km        numeric(8, 2) not null,
    duracao_minutos     integer not null,
    -- De onde vieram a distância e o tempo (FonteDoTrajeto).
    fonte_trajeto       text not null,
    despachada_em       timestamptz not null default now(),
    chegada_prevista_em timestamptz not null,
    recebida_em         timestamptz,
    despachada_por      uuid,
    recebida_por        uuid,
    constraint fk_transferencia_bolsa
        foreign key (bolsa_id) references public.bolsa_hemocomponente (id) on delete restrict,
    constraint ck_transferencia_origem_tipo check (origem_tipo = 'BANCO_DE_SANGUE'),
    constraint ck_transferencia_destino_tipo check (destino_tipo = 'HOSPITAL'),
    constraint fk_transferencia_origem_e_banco_de_sangue
        foreign key (origem_id, origem_tipo) references public.ponto_rede (id, tipo) on delete restrict,
    constraint fk_transferencia_destino_e_hospital
        foreign key (destino_id, destino_tipo) references public.ponto_rede (id, tipo) on delete restrict,
    constraint fk_transferencia_despachada_por
        foreign key (despachada_por) references public.usuario (id) on delete set null,
    constraint fk_transferencia_recebida_por
        foreign key (recebida_por) references public.usuario (id) on delete set null,
    constraint ck_transferencia_status check (status in ('EM_TRANSITO', 'RECEBIDA', 'CANCELADA')),
    constraint ck_transferencia_distancia check (distancia_km >= 0),
    constraint ck_transferencia_duracao check (duracao_minutos >= 0),
    constraint ck_transferencia_fonte check (fonte_trajeto in ('GOOGLE_ROUTES', 'ESTIMATIVA', 'CONEXOES_DA_REDE')),
    constraint ck_transferencia_chegada_prevista check (chegada_prevista_em >= despachada_em),
    constraint ck_transferencia_recebida_apos_saida check (recebida_em is null or recebida_em >= despachada_em),
    constraint ck_transferencia_recebida_tem_data check ((status = 'RECEBIDA') = (recebida_em is not null))
);

-- Uma bolsa só pode estar em uma transferência ativa: dois envios simultâneos não passam.
create unique index uq_transferencia_bolsa_ativa on public.transferencia_bolsa (bolsa_id)
    where status = 'EM_TRANSITO';

create index idx_transferencia_status on public.transferencia_bolsa (status, despachada_em);
create index idx_transferencia_destino on public.transferencia_bolsa (destino_id);
create index idx_transferencia_despachada_por on public.transferencia_bolsa (despachada_por);
create index idx_transferencia_recebida_por on public.transferencia_bolsa (recebida_por);

-- RLS ligado sem políticas: bloqueia a API REST pública; o backend usa o role postgres.
alter table public.transferencia_bolsa enable row level security;
