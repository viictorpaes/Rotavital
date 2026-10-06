-- ===================================================================
-- Rota Vital — Escopo clínico do MVP, conforme docs/DER.md:
-- usuario, paciente, remessa, campanha_doacao, agendamento_doacao,
-- procedimento e procedimento_bolsa, com constraints, gatilhos e RLS.
-- No fim, as FKs de bolsa_hemocomponente e requisicao_hospitalar que
-- apontam para as tabelas criadas aqui.
-- ===================================================================

-- -------------------------------------------------------------------
-- usuario — médico e doador (HU-01, HU-10)
-- -------------------------------------------------------------------
create table public.usuario
(
    id             uuid primary key default gen_random_uuid(),
    auth_user_id   uuid,
    nome           text not null,
    papel          text not null,
    ponto_rede_id  text,
    tipo_sanguineo text,
    criado_em      timestamptz not null default now(),
    constraint uq_usuario_auth_user unique (auth_user_id),
    constraint fk_usuario_auth_user
        foreign key (auth_user_id) references auth.users (id) on delete set null,
    constraint ck_usuario_nome_preenchido check (btrim(nome) <> ''),
    constraint ck_usuario_papel check (papel in ('MEDICO', 'DOADOR')),
    constraint fk_usuario_ponto_rede
        foreign key (ponto_rede_id) references public.ponto_rede (id) on delete restrict,
    constraint ck_usuario_tipo_sanguineo check (tipo_sanguineo in ('A_POSITIVO', 'A_NEGATIVO', 'B_POSITIVO', 'B_NEGATIVO',
                                                                   'AB_POSITIVO', 'AB_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO')),
    -- Alvo das FKs compostas que garantem "este usuário é MEDICO / DOADOR".
    constraint uq_usuario_id_papel unique (id, papel)
);

create index idx_usuario_ponto_rede on public.usuario (ponto_rede_id);

-- -------------------------------------------------------------------
-- paciente — pessoa que precisa de hemocomponente (HU-02, HU-09)
-- -------------------------------------------------------------------
create table public.paciente
(
    id                   uuid primary key default gen_random_uuid(),
    hospital_id          text not null,
    hospital_tipo        text not null default 'HOSPITAL',
    nome                 text not null,
    sexo                 text not null,
    data_nascimento      date not null,
    tipo_sanguineo       text not null,
    tipo_componente      text not null,
    unidades_necessarias integer not null,
    causa                text not null,
    gravidade            text not null,
    status               text not null default 'AGUARDANDO',
    criado_em            timestamptz not null default now(),
    atualizado_em        timestamptz not null default now(),
    constraint ck_paciente_hospital_tipo check (hospital_tipo = 'HOSPITAL'),
    constraint fk_paciente_hospital_e_hospital
        foreign key (hospital_id, hospital_tipo) references public.ponto_rede (id, tipo) on delete restrict,
    constraint ck_paciente_nome_preenchido check (btrim(nome) <> ''),
    constraint ck_paciente_sexo check (sexo in ('FEMININO', 'MASCULINO')),
    -- Data futura é validada no backend (current_date não é imutável para um CHECK).
    constraint ck_paciente_data_nascimento check (data_nascimento > date '1900-01-01'),
    constraint ck_paciente_tipo_sanguineo check (tipo_sanguineo in ('A_POSITIVO', 'A_NEGATIVO', 'B_POSITIVO', 'B_NEGATIVO',
                                                                    'AB_POSITIVO', 'AB_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO')),
    constraint ck_paciente_tipo_componente check (tipo_componente in ('HEMACIAS', 'PLASMA', 'PLAQUETAS', 'CRIOPRECIPITADO')),
    constraint ck_paciente_unidades_necessarias check (unidades_necessarias between 1 and 50),
    constraint ck_paciente_causa_preenchida check (btrim(causa) <> ''),
    constraint ck_paciente_gravidade check (gravidade in ('CRITICO', 'ATENCAO', 'ESTAVEL')),
    constraint ck_paciente_status check (status in ('AGUARDANDO', 'ATENDIDO', 'CANCELADO')),
    -- Alvo da FK de requisicao_hospitalar: o paciente é do hospital que faz o pedido.
    constraint uq_paciente_id_hospital unique (id, hospital_id)
);

create index idx_paciente_hospital on public.paciente (hospital_id);
-- Fila do painel: aguardando, dos mais graves e mais antigos primeiro.
create index idx_paciente_fila on public.paciente (gravidade, criado_em) where status = 'AGUARDANDO';

create trigger trg_paciente_atualizado_em
    before update on public.paciente
    for each row
execute function public.fn_atualizar_atualizado_em();

-- -------------------------------------------------------------------
-- remessa — chegada de outra instituição (HU-05)
-- -------------------------------------------------------------------
create table public.remessa
(
    id                  uuid primary key default gen_random_uuid(),
    codigo              text not null,
    origem_id           text,
    origem_tipo         text not null default 'BANCO_DE_SANGUE',
    origem_descricao    text,
    destino_id          text not null,
    destino_tipo        text not null default 'BANCO_DE_SANGUE',
    tipo_componente     text not null,
    tipo_sanguineo      text not null,
    unidades            integer not null,
    chegada_prevista_em timestamptz not null,
    recebida_em         timestamptz,
    conferida_por       uuid,
    status              text not null default 'EM_TRANSITO',
    constraint uq_remessa_codigo unique (codigo),
    constraint ck_remessa_codigo_preenchido check (btrim(codigo) <> ''),
    constraint ck_remessa_origem_tipo check (origem_tipo = 'BANCO_DE_SANGUE'),
    constraint ck_remessa_destino_tipo check (destino_tipo = 'BANCO_DE_SANGUE'),
    -- Com origem_id nulo (doação coletiva) a FK composta não se aplica.
    constraint fk_remessa_origem_e_banco_de_sangue
        foreign key (origem_id, origem_tipo) references public.ponto_rede (id, tipo) on delete restrict,
    constraint fk_remessa_destino_e_banco_de_sangue
        foreign key (destino_id, destino_tipo) references public.ponto_rede (id, tipo) on delete restrict,
    constraint fk_remessa_conferente
        foreign key (conferida_por) references public.usuario (id) on delete set null,
    constraint ck_remessa_origem_informada check (origem_id is not null or coalesce(btrim(origem_descricao), '') <> ''),
    constraint ck_remessa_origem_diferente_destino check (origem_id is null or origem_id <> destino_id),
    constraint ck_remessa_tipo_componente check (tipo_componente in ('HEMACIAS', 'PLASMA', 'PLAQUETAS', 'CRIOPRECIPITADO')),
    constraint ck_remessa_tipo_sanguineo check (tipo_sanguineo in ('A_POSITIVO', 'A_NEGATIVO', 'B_POSITIVO', 'B_NEGATIVO',
                                                                   'AB_POSITIVO', 'AB_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO')),
    constraint ck_remessa_unidades check (unidades between 1 and 200),
    constraint ck_remessa_status check (status in ('EM_TRANSITO', 'RECEBIDA', 'RECUSADA')),
    constraint ck_remessa_recebida_conferida check (status <> 'RECEBIDA'
                                                    or (recebida_em is not null and conferida_por is not null)),
    -- Alvo da FK da bolsa: bolsa gerada fica no banco de destino, com o mesmo componente e tipo.
    constraint uq_remessa_id_destino_componente_sanguineo unique (id, destino_id, tipo_componente, tipo_sanguineo)
);

create index idx_remessa_origem on public.remessa (origem_id);
create index idx_remessa_destino on public.remessa (destino_id);
create index idx_remessa_conferente on public.remessa (conferida_por);

-- -------------------------------------------------------------------
-- campanha_doacao — paciente divulgado para doação (HU-06)
-- -------------------------------------------------------------------
create table public.campanha_doacao
(
    id                  uuid primary key default gen_random_uuid(),
    paciente_id         uuid not null,
    publicada_por       uuid,
    publicada_por_papel text not null default 'MEDICO',
    publicada_em        timestamptz not null default now(),
    status              text not null default 'ATIVA',
    encerrada_em        timestamptz,
    constraint fk_campanha_paciente
        foreign key (paciente_id) references public.paciente (id) on delete cascade,
    constraint ck_campanha_publicada_por_papel check (publicada_por_papel = 'MEDICO'),
    constraint fk_campanha_publicada_por_medico
        foreign key (publicada_por, publicada_por_papel) references public.usuario (id, papel) on delete restrict,
    constraint ck_campanha_status check (status in ('ATIVA', 'ENCERRADA')),
    constraint ck_campanha_encerrada_em check ((status = 'ENCERRADA') = (encerrada_em is not null)),
    constraint ck_campanha_encerrada_apos_publicada check (encerrada_em is null or encerrada_em >= publicada_em)
);

-- Uma campanha ativa por paciente; depois de encerrada pode ser publicada de novo.
create unique index uq_campanha_paciente_ativa on public.campanha_doacao (paciente_id)
    where status = 'ATIVA';
create index idx_campanha_paciente on public.campanha_doacao (paciente_id);
create index idx_campanha_publicada_por on public.campanha_doacao (publicada_por);

-- -------------------------------------------------------------------
-- agendamento_doacao — HU-09 / HU-10
-- -------------------------------------------------------------------
create table public.agendamento_doacao
(
    id            uuid primary key default gen_random_uuid(),
    doador_id     uuid not null,
    doador_papel  text not null default 'DOADOR',
    banco_id      text not null,
    banco_tipo    text not null default 'BANCO_DE_SANGUE',
    campanha_id   uuid,
    agendado_para timestamptz not null,
    status        text not null default 'AGENDADO',
    criado_em     timestamptz not null default now(),
    constraint ck_agendamento_doador_papel check (doador_papel = 'DOADOR'),
    constraint fk_agendamento_doador_e_doador
        foreign key (doador_id, doador_papel) references public.usuario (id, papel) on delete restrict,
    constraint ck_agendamento_banco_tipo check (banco_tipo = 'BANCO_DE_SANGUE'),
    constraint fk_agendamento_banco_e_banco_de_sangue
        foreign key (banco_id, banco_tipo) references public.ponto_rede (id, tipo) on delete restrict,
    constraint fk_agendamento_campanha
        foreign key (campanha_id) references public.campanha_doacao (id) on delete set null,
    constraint ck_agendamento_no_futuro check (agendado_para > criado_em),
    constraint ck_agendamento_status check (status in ('AGENDADO', 'REALIZADO', 'CANCELADO', 'FALTOU'))
);

-- Um agendamento em aberto por doador.
create unique index uq_agendamento_doador_aberto on public.agendamento_doacao (doador_id)
    where status = 'AGENDADO';
create index idx_agendamento_doador on public.agendamento_doacao (doador_id);
create index idx_agendamento_banco on public.agendamento_doacao (banco_id, agendado_para);
create index idx_agendamento_campanha on public.agendamento_doacao (campanha_id);

-- -------------------------------------------------------------------
-- procedimento — conclusão do procedimento (HU-08)
-- -------------------------------------------------------------------
create table public.procedimento
(
    id                 uuid primary key default gen_random_uuid(),
    protocolo          text not null,
    paciente_id        uuid not null,
    origem             text not null,
    unidades_previstas integer not null,
    unidades_faltantes integer not null default 0,
    responsavel_id     uuid,
    responsavel_papel  text not null default 'MEDICO',
    concluido_em       timestamptz not null default now(),
    constraint uq_procedimento_protocolo unique (protocolo),
    constraint ck_procedimento_protocolo_preenchido check (btrim(protocolo) <> ''),
    constraint fk_procedimento_paciente
        foreign key (paciente_id) references public.paciente (id) on delete restrict,
    constraint ck_procedimento_origem check (origem in ('ESTOQUE_INTERNO', 'DOACAO_EXTERNA')),
    constraint ck_procedimento_unidades_previstas check (unidades_previstas > 0),
    constraint ck_procedimento_unidades_faltantes check (unidades_faltantes between 0 and unidades_previstas),
    constraint ck_procedimento_responsavel_papel check (responsavel_papel = 'MEDICO'),
    constraint fk_procedimento_responsavel_medico
        foreign key (responsavel_id, responsavel_papel) references public.usuario (id, papel) on delete restrict
);

create index idx_procedimento_paciente on public.procedimento (paciente_id);
create index idx_procedimento_responsavel on public.procedimento (responsavel_id);

-- -------------------------------------------------------------------
-- procedimento_bolsa — baixa FEFO por bolsa
-- -------------------------------------------------------------------
create table public.procedimento_bolsa
(
    procedimento_id uuid not null,
    bolsa_id        text not null,
    constraint pk_procedimento_bolsa primary key (procedimento_id, bolsa_id),
    constraint fk_procedimento_bolsa_procedimento
        foreign key (procedimento_id) references public.procedimento (id) on delete cascade,
    constraint fk_procedimento_bolsa_bolsa
        foreign key (bolsa_id) references public.bolsa_hemocomponente (id) on delete restrict,
    -- Uma bolsa é transfundida uma única vez.
    constraint uq_procedimento_bolsa_bolsa unique (bolsa_id)
);

-- Regras que dependem de outras linhas:
--   * só procedimento com origem ESTOQUE_INTERNO dá baixa em bolsa;
--   * a bolsa é do componente de que o paciente precisa;
--   * a bolsa não está vencida, descartada nem já utilizada.
-- Passando, a bolsa é marcada como UTILIZADA.
create or replace function public.fn_procedimento_bolsa_validar()
    returns trigger
    language plpgsql
    set search_path = ''
as
$$
declare
    v_origem          text;
    v_tipo_componente text;
    v_bolsa           public.bolsa_hemocomponente%rowtype;
begin
    select p.origem, pa.tipo_componente
    into v_origem, v_tipo_componente
    from public.procedimento p
             join public.paciente pa on pa.id = p.paciente_id
    where p.id = new.procedimento_id;

    if not found then
        raise exception 'Procedimento % não encontrado', new.procedimento_id
            using errcode = 'foreign_key_violation', constraint = 'fk_procedimento_bolsa_procedimento';
    end if;

    if v_origem <> 'ESTOQUE_INTERNO' then
        raise exception 'Procedimento % é de doação externa e não dá baixa em bolsa', new.procedimento_id
            using errcode = 'check_violation', constraint = 'ck_procedimento_bolsa_origem_estoque';
    end if;

    select * into v_bolsa
    from public.bolsa_hemocomponente
    where id = new.bolsa_id
    for update;

    if not found then
        raise exception 'Bolsa % não encontrada', new.bolsa_id
            using errcode = 'foreign_key_violation', constraint = 'fk_procedimento_bolsa_bolsa';
    end if;

    if v_bolsa.tipo_componente <> v_tipo_componente then
        raise exception 'Bolsa % é de %, mas o paciente precisa de %', v_bolsa.id, v_bolsa.tipo_componente, v_tipo_componente
            using errcode = 'check_violation', constraint = 'ck_procedimento_bolsa_componente';
    end if;

    if v_bolsa.status in ('DESCARTADA', 'UTILIZADA') then
        raise exception 'Bolsa % está % e não pode ser transfundida', v_bolsa.id, v_bolsa.status
            using errcode = 'check_violation', constraint = 'ck_procedimento_bolsa_utilizavel';
    end if;

    if v_bolsa.data_validade < current_date then
        raise exception 'Bolsa % venceu em %', v_bolsa.id, v_bolsa.data_validade
            using errcode = 'check_violation', constraint = 'ck_procedimento_bolsa_dentro_validade';
    end if;

    update public.bolsa_hemocomponente
    set status = 'UTILIZADA'
    where id = new.bolsa_id;

    return new;
end;
$$;

create trigger trg_procedimento_bolsa_validar
    before insert on public.procedimento_bolsa
    for each row
execute function public.fn_procedimento_bolsa_validar();

-- -------------------------------------------------------------------
-- FKs das tabelas existentes para as tabelas novas
-- -------------------------------------------------------------------
alter table public.bolsa_hemocomponente
    -- Com remessa_id nulo a FK não se aplica.
    add constraint fk_bolsa_remessa_compativel
        foreign key (remessa_id, banco_origem_id, tipo_componente, tipo_sanguineo)
        references public.remessa (id, destino_id, tipo_componente, tipo_sanguineo) on delete restrict;

alter table public.requisicao_hospitalar
    -- O paciente é do hospital que faz a requisição.
    add constraint fk_requisicao_paciente_do_hospital
        foreign key (paciente_id, hospital_id) references public.paciente (id, hospital_id) on delete restrict,
    add constraint fk_requisicao_solicitante
        foreign key (solicitante_id) references public.usuario (id) on delete set null;

-- -------------------------------------------------------------------
-- RLS ligado sem políticas: bloqueia a API REST pública; o backend usa o role postgres.
-- -------------------------------------------------------------------
alter table public.usuario            enable row level security;
alter table public.paciente           enable row level security;
alter table public.remessa            enable row level security;
alter table public.campanha_doacao    enable row level security;
alter table public.agendamento_doacao enable row level security;
alter table public.procedimento       enable row level security;
alter table public.procedimento_bolsa enable row level security;
