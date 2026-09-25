-- ===================================================================
-- Rota Vital — Ajustes nas 7 tabelas já aplicadas (PI3-139), conforme docs/DER.md.
--   * colunas novas em ponto_rede, bolsa_hemocomponente, requisicao_hospitalar e alocacao;
--   * status UTILIZADA na bolsa;
--   * alocação ativa única por bolsa (índice parcial) e gatilho contando só as ativas;
--   * gatilho genérico de atualizado_em;
--   * remoção das FKs simples redundantes com as FKs compostas.
-- As FKs que apontam para tabelas novas (usuario, paciente, remessa) entram no fim
-- da migration seguinte: 20260925120100_escopo_clinico.sql
-- ===================================================================

-- -------------------------------------------------------------------
-- Gatilho genérico: atualizado_em = now() em todo UPDATE.
-- -------------------------------------------------------------------
create or replace function public.fn_atualizar_atualizado_em()
    returns trigger
    language plpgsql
    set search_path = ''
as
$$
begin
    new.atualizado_em := now();
    return new;
end;
$$;

-- -------------------------------------------------------------------
-- ponto_rede
-- -------------------------------------------------------------------
alter table public.ponto_rede
    add column cidade                text,
    add column uf                    char(2),
    add column horario_funcionamento text,
    add constraint ck_ponto_rede_uf check (uf ~ '^[A-Z]{2}$');

-- -------------------------------------------------------------------
-- bolsa_hemocomponente
-- -------------------------------------------------------------------
alter table public.bolsa_hemocomponente
    drop constraint ck_bolsa_status,
    -- UTILIZADA = transfundida em um procedimento; ENTREGUE é só a chegada ao hospital.
    add constraint ck_bolsa_status check (status in ('DISPONIVEL', 'RESERVADA', 'EM_TRANSITO', 'ENTREGUE',
                                                     'DESCARTADA', 'UTILIZADA')),
    -- FK composta para remessa é criada na migration seguinte, depois da tabela existir.
    add column remessa_id    uuid,
    add column atualizado_em timestamptz not null default now(),
    -- Redundante com fk_bolsa_banco_origem_e_banco_de_sangue.
    drop constraint bolsa_hemocomponente_banco_origem_id_fkey;

create index idx_bolsa_remessa on public.bolsa_hemocomponente (remessa_id);

create trigger trg_bolsa_hemocomponente_atualizado_em
    before update on public.bolsa_hemocomponente
    for each row
execute function public.fn_atualizar_atualizado_em();

-- -------------------------------------------------------------------
-- requisicao_hospitalar
-- -------------------------------------------------------------------
alter table public.requisicao_hospitalar
    add column protocolo      text not null,
    add column banco_id       text not null,
    add column banco_tipo     text not null default 'BANCO_DE_SANGUE',
    -- FKs de paciente_id e solicitante_id são criadas na migration seguinte.
    add column paciente_id    uuid,
    add column ala            text,
    add column solicitante_id uuid,
    add column atualizado_em  timestamptz not null default now(),
    add constraint uq_requisicao_protocolo unique (protocolo),
    add constraint ck_requisicao_protocolo_preenchido check (btrim(protocolo) <> ''),
    add constraint ck_requisicao_banco_tipo check (banco_tipo = 'BANCO_DE_SANGUE'),
    add constraint fk_requisicao_banco_e_banco_de_sangue
        foreign key (banco_id, banco_tipo) references public.ponto_rede (id, tipo) on delete restrict,
    add constraint ck_requisicao_ala check (ala in ('UTI_ADULTO', 'UTI_NEONATAL', 'CENTRO_CIRURGICO', 'EMERGENCIA',
                                                    'ONCOLOGIA', 'HEMODIALISE', 'MATERNIDADE')),
    -- Redundante com fk_requisicao_hospital_e_hospital.
    drop constraint requisicao_hospitalar_hospital_id_fkey;

create index idx_requisicao_banco on public.requisicao_hospitalar (banco_id);
create index idx_requisicao_paciente on public.requisicao_hospitalar (paciente_id);
create index idx_requisicao_solicitante on public.requisicao_hospitalar (solicitante_id);

create trigger trg_requisicao_hospitalar_atualizado_em
    before update on public.requisicao_hospitalar
    for each row
execute function public.fn_atualizar_atualizado_em();

-- -------------------------------------------------------------------
-- alocacao
-- -------------------------------------------------------------------
alter table public.alocacao
    add column cancelada_em timestamptz,
    add constraint ck_alocacao_cancelada_apos_alocacao check (cancelada_em is null or cancelada_em >= data_alocacao),
    -- Substituída pelo índice parcial abaixo: a bolsa pode ser realocada depois de um cancelamento.
    drop constraint uq_alocacao_bolsa,
    -- Redundantes com fk_alocacao_requisicao_compativel / fk_alocacao_bolsa_compativel.
    drop constraint alocacao_requisicao_id_fkey,
    drop constraint alocacao_bolsa_id_fkey;

create unique index uq_alocacao_bolsa_ativa on public.alocacao (bolsa_id)
    where cancelada_em is null;

-- Mesmas regras da migration anterior; a quantidade agora conta só as alocações ativas.
create or replace function public.fn_alocacao_validar()
    returns trigger
    language plpgsql
    set search_path = ''
as
$$
declare
    v_requisicao public.requisicao_hospitalar%rowtype;
    v_bolsa      public.bolsa_hemocomponente%rowtype;
    v_alocadas   integer;
begin
    -- FOR UPDATE serializa alocações concorrentes da mesma requisição.
    select * into v_requisicao
    from public.requisicao_hospitalar
    where id = new.requisicao_id
    for update;

    if not found then
        raise exception 'Requisição % não encontrada', new.requisicao_id
            using errcode = 'foreign_key_violation', constraint = 'fk_alocacao_requisicao_compativel';
    end if;

    new.tipo_componente := coalesce(new.tipo_componente, v_requisicao.tipo_componente);
    new.tipo_sanguineo  := coalesce(new.tipo_sanguineo, v_requisicao.tipo_sanguineo);

    if v_requisicao.status not in ('PENDENTE', 'ALOCADA') then
        raise exception 'Requisição % está % e não aceita alocação', v_requisicao.id, v_requisicao.status
            using errcode = 'check_violation', constraint = 'ck_alocacao_requisicao_aberta';
    end if;

    select count(*) into v_alocadas
    from public.alocacao
    where requisicao_id = new.requisicao_id
      and cancelada_em is null;

    if v_alocadas >= v_requisicao.quantidade then
        raise exception 'Requisição % já tem as % bolsas pedidas', v_requisicao.id, v_requisicao.quantidade
            using errcode = 'check_violation', constraint = 'ck_alocacao_quantidade_maxima';
    end if;

    select * into v_bolsa
    from public.bolsa_hemocomponente
    where id = new.bolsa_id
    for update;

    if found then
        if v_bolsa.status <> 'DISPONIVEL' then
            raise exception 'Bolsa % está % e não pode ser alocada', v_bolsa.id, v_bolsa.status
                using errcode = 'check_violation', constraint = 'ck_alocacao_bolsa_disponivel';
        end if;

        if v_bolsa.data_validade < current_date then
            raise exception 'Bolsa % venceu em %', v_bolsa.id, v_bolsa.data_validade
                using errcode = 'check_violation', constraint = 'ck_alocacao_bolsa_dentro_validade';
        end if;
    end if;

    return new;
end;
$$;

-- -------------------------------------------------------------------
-- entrega — FKs simples redundantes com as compostas.
-- -------------------------------------------------------------------
alter table public.entrega
    drop constraint entrega_origem_id_fkey,
    drop constraint entrega_destino_id_fkey,
    -- Coberta por fk_entrega_destino_e_hospital_da_requisicao (requisicao_id, destino_id).
    drop constraint entrega_requisicao_id_fkey;
