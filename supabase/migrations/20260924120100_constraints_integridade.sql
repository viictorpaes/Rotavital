-- ===================================================================
-- Rota Vital — Constraints de integridade (NOT NULL, UNIQUE, CHECK, DEFAULT,
-- FKs de regra de negócio e gatilho de alocação), conforme docs/DER.md.
--
-- Convenção de nomes: ck_<tabela>_<regra>, uq_<tabela>_<colunas>,
-- fk_<tabela>_<regra>. O nome aparece na mensagem de erro do Postgres, então
-- o backend consegue traduzir a violação para um 422 legível.
-- ===================================================================

-- -------------------------------------------------------------------
-- ponto_rede
-- -------------------------------------------------------------------
alter table public.ponto_rede
    alter column tipo       set not null,
    alter column nome       set not null,
    alter column logradouro set not null,
    alter column latitude   set not null,
    alter column longitude  set not null,
    alter column criado_em  set default now(),
    alter column criado_em  set not null,
    add constraint ck_ponto_rede_id_formato      check (id ~ '^[A-Za-z0-9_-]{2,40}$'),
    add constraint ck_ponto_rede_tipo            check (tipo in ('HOSPITAL', 'BANCO_DE_SANGUE')),
    add constraint ck_ponto_rede_nome_preenchido check (btrim(nome) <> ''),
    add constraint ck_ponto_rede_logradouro_preenchido check (btrim(logradouro) <> ''),
    add constraint ck_ponto_rede_latitude        check (latitude between -90 and 90),
    add constraint ck_ponto_rede_longitude       check (longitude between -180 and 180),
    add constraint uq_ponto_rede_tipo_nome       unique (tipo, nome),
    -- Alvo das FKs compostas que garantem "este id é um HOSPITAL / BANCO_DE_SANGUE".
    add constraint uq_ponto_rede_id_tipo         unique (id, tipo);

-- -------------------------------------------------------------------
-- conexao (aresta do grafo usado pelo Dijkstra)
-- -------------------------------------------------------------------
alter table public.conexao
    alter column origem_id          set not null,
    alter column destino_id         set not null,
    alter column distancia_km       set not null,
    alter column tempo_estimado_min set not null,
    add constraint ck_conexao_sem_laco        check (origem_id <> destino_id),
    -- Peso zero ou negativo quebra o Dijkstra.
    add constraint ck_conexao_distancia_positiva check (distancia_km > 0),
    add constraint ck_conexao_tempo_positivo     check (tempo_estimado_min > 0),
    add constraint uq_conexao_origem_destino     unique (origem_id, destino_id);

-- -------------------------------------------------------------------
-- bolsa_hemocomponente
-- -------------------------------------------------------------------
alter table public.bolsa_hemocomponente
    -- Coluna fixa usada só pela FK composta: a origem de uma bolsa é sempre um banco de sangue.
    add column banco_origem_tipo text not null default 'BANCO_DE_SANGUE',
    alter column banco_origem_id set not null,
    alter column tipo_componente set not null,
    alter column tipo_sanguineo  set not null,
    alter column data_coleta     set not null,
    alter column data_validade   set not null,
    alter column lote_sintetico  set not null,
    alter column volume_ml       set not null,
    alter column status          set default 'DISPONIVEL',
    alter column status          set not null,
    alter column criado_em       set default now(),
    alter column criado_em       set not null,
    add constraint ck_bolsa_banco_origem_tipo check (banco_origem_tipo = 'BANCO_DE_SANGUE'),
    add constraint fk_bolsa_banco_origem_e_banco_de_sangue
        foreign key (banco_origem_id, banco_origem_tipo)
        references public.ponto_rede (id, tipo) on delete restrict,
    add constraint ck_bolsa_id_formato      check (id ~ '^[A-Za-z0-9_-]{2,40}$'),
    add constraint ck_bolsa_tipo_componente check (tipo_componente in ('HEMACIAS', 'PLASMA', 'PLAQUETAS', 'CRIOPRECIPITADO')),
    add constraint ck_bolsa_tipo_sanguineo  check (tipo_sanguineo in ('A_POSITIVO', 'A_NEGATIVO', 'B_POSITIVO', 'B_NEGATIVO',
                                                                      'AB_POSITIVO', 'AB_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO')),
    add constraint ck_bolsa_status          check (status in ('DISPONIVEL', 'RESERVADA', 'EM_TRANSITO', 'ENTREGUE', 'DESCARTADA')),
    add constraint ck_bolsa_validade_apos_coleta check (data_validade > data_coleta),
    -- Prazo máximo de armazenamento por componente (RDC ANVISA 34/2014):
    -- hemácias 42 d (SAG-M), plaquetas 5 d (+2 com teste bacteriológico),
    -- crioprecipitado 1 ano, plasma congelado até 2 anos (abaixo de -25 °C).
    add constraint ck_bolsa_prazo_maximo_por_componente check (
        data_validade - data_coleta <= case tipo_componente
                                           when 'HEMACIAS'        then 42
                                           when 'PLAQUETAS'       then 7
                                           when 'CRIOPRECIPITADO' then 365
                                           when 'PLASMA'          then 730
                                       end),
    add constraint ck_bolsa_volume_ml       check (volume_ml > 0 and volume_ml <= 700),
    -- Faixa física do sensor, não a faixa ideal: bolsa fora da faixa ideal é alerta
    -- (BolsaHemocomponente.estaForaDaFaixa), não dado inválido.
    add constraint ck_bolsa_temperatura_sensor check (temperatura_celsius between -80 and 60),
    add constraint ck_bolsa_lote_preenchido check (btrim(lote_sintetico) <> ''),
    -- Uma doação gera no máximo uma bolsa de cada componente.
    add constraint uq_bolsa_lote_componente unique (lote_sintetico, tipo_componente),
    -- Alvo da FK composta da alocação (bolsa e requisição com o mesmo componente/tipo).
    add constraint uq_bolsa_id_componente_sanguineo unique (id, tipo_componente, tipo_sanguineo);

-- Acelera a busca FEFO: disponíveis de um componente/tipo, da validade mais próxima para a mais distante.
create index idx_bolsa_fefo on public.bolsa_hemocomponente (tipo_componente, tipo_sanguineo, data_validade)
    where status = 'DISPONIVEL';

-- -------------------------------------------------------------------
-- requisicao_hospitalar
-- -------------------------------------------------------------------
alter table public.requisicao_hospitalar
    add column hospital_tipo text not null default 'HOSPITAL',
    alter column hospital_id      set not null,
    alter column tipo_componente  set not null,
    alter column tipo_sanguineo   set not null,
    alter column quantidade       set not null,
    alter column urgencia         set default 'MEDIA',
    alter column urgencia         set not null,
    alter column data_solicitacao set default now(),
    alter column data_solicitacao set not null,
    alter column status           set default 'PENDENTE',
    alter column status           set not null,
    add constraint ck_requisicao_hospital_tipo check (hospital_tipo = 'HOSPITAL'),
    add constraint fk_requisicao_hospital_e_hospital
        foreign key (hospital_id, hospital_tipo)
        references public.ponto_rede (id, tipo) on delete restrict,
    add constraint ck_requisicao_tipo_componente check (tipo_componente in ('HEMACIAS', 'PLASMA', 'PLAQUETAS', 'CRIOPRECIPITADO')),
    add constraint ck_requisicao_tipo_sanguineo  check (tipo_sanguineo in ('A_POSITIVO', 'A_NEGATIVO', 'B_POSITIVO', 'B_NEGATIVO',
                                                                           'AB_POSITIVO', 'AB_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO')),
    add constraint ck_requisicao_quantidade check (quantidade between 1 and 100),
    add constraint ck_requisicao_urgencia   check (urgencia in ('BAIXA', 'MEDIA', 'ALTA')),
    add constraint ck_requisicao_status     check (status in ('PENDENTE', 'ALOCADA', 'EM_TRANSITO', 'ENTREGUE', 'CANCELADA')),
    add constraint uq_requisicao_id_componente_sanguineo unique (id, tipo_componente, tipo_sanguineo),
    add constraint uq_requisicao_id_hospital unique (id, hospital_id);

-- -------------------------------------------------------------------
-- alocacao
-- -------------------------------------------------------------------
alter table public.alocacao
    -- Copiados da requisição pelo gatilho abaixo; existem para as FKs compostas.
    add column tipo_componente text,
    add column tipo_sanguineo  text,
    alter column requisicao_id set not null,
    alter column bolsa_id      set not null,
    alter column data_alocacao set default now(),
    alter column data_alocacao set not null;

alter table public.alocacao
    alter column tipo_componente set not null,
    alter column tipo_sanguineo  set not null,
    -- Uma bolsa física atende uma única requisição.
    add constraint uq_alocacao_bolsa unique (bolsa_id),
    -- Compatibilidade exigida pelo FEFO atual (Estoque.buscarDisponiveis):
    -- a bolsa tem de ser do mesmo componente e do mesmo tipo sanguíneo pedido.
    add constraint fk_alocacao_requisicao_compativel
        foreign key (requisicao_id, tipo_componente, tipo_sanguineo)
        references public.requisicao_hospitalar (id, tipo_componente, tipo_sanguineo) on delete cascade,
    add constraint fk_alocacao_bolsa_compativel
        foreign key (bolsa_id, tipo_componente, tipo_sanguineo)
        references public.bolsa_hemocomponente (id, tipo_componente, tipo_sanguineo) on delete restrict;

-- Regras que dependem de outras linhas (CHECK não enxerga outras tabelas):
--   * a bolsa precisa estar DISPONIVEL e dentro da validade no momento da alocação;
--   * a requisição precisa estar PENDENTE ou ALOCADA;
--   * não se aloca mais bolsas do que a quantidade pedida.
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
    where requisicao_id = new.requisicao_id;

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

create trigger trg_alocacao_validar
    before insert on public.alocacao
    for each row
execute function public.fn_alocacao_validar();

-- -------------------------------------------------------------------
-- entrega
-- -------------------------------------------------------------------
alter table public.entrega
    add column origem_tipo  text not null default 'BANCO_DE_SANGUE',
    add column destino_tipo text not null default 'HOSPITAL',
    alter column requisicao_id set not null,
    alter column origem_id     set not null,
    alter column destino_id    set not null,
    alter column status        set default 'EM_TRANSITO',
    alter column status        set not null,
    alter column saida_em      set default now(),
    alter column saida_em      set not null,
    add constraint ck_entrega_origem_tipo  check (origem_tipo = 'BANCO_DE_SANGUE'),
    add constraint ck_entrega_destino_tipo check (destino_tipo = 'HOSPITAL'),
    add constraint fk_entrega_origem_e_banco_de_sangue
        foreign key (origem_id, origem_tipo) references public.ponto_rede (id, tipo) on delete restrict,
    add constraint fk_entrega_destino_e_hospital
        foreign key (destino_id, destino_tipo) references public.ponto_rede (id, tipo) on delete restrict,
    -- A entrega vai para o hospital que fez a requisição.
    add constraint fk_entrega_destino_e_hospital_da_requisicao
        foreign key (requisicao_id, destino_id)
        references public.requisicao_hospitalar (id, hospital_id) on delete restrict,
    add constraint ck_entrega_status check (status in ('EM_TRANSITO', 'ENTREGUE', 'CANCELADA')),
    add constraint ck_entrega_chegada_apos_saida check (chegada_em is null or chegada_em >= saida_em),
    add constraint ck_entrega_entregue_tem_chegada check (status <> 'ENTREGUE' or chegada_em is not null);

-- Só uma entrega ativa por requisição; uma cancelada pode ser despachada de novo.
create unique index uq_entrega_requisicao_ativa on public.entrega (requisicao_id)
    where status <> 'CANCELADA';

-- -------------------------------------------------------------------
-- leitura_telemetria
-- -------------------------------------------------------------------
alter table public.leitura_telemetria
    alter column entrega_id          set not null,
    alter column registrado_em       set not null,
    alter column latitude            set not null,
    alter column longitude           set not null,
    alter column temperatura_celsius set not null,
    add constraint ck_leitura_latitude    check (latitude between -90 and 90),
    add constraint ck_leitura_longitude   check (longitude between -180 and 180),
    add constraint ck_leitura_temperatura_sensor check (temperatura_celsius between -80 and 60),
    -- Um sensor não emite duas leituras no mesmo instante para a mesma entrega;
    -- também serve de índice para o histórico em ordem cronológica.
    add constraint uq_leitura_entrega_instante unique (entrega_id, registrado_em);
