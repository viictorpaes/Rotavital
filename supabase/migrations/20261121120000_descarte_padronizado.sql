-- PI3-243: o descarte passa a ter um motivo padronizado (para contar as perdas por motivo)
-- e a data em que foi registrado (para o painel de perdas por período). O texto livre de
-- motivo_descarte continua como observação.
alter table public.bolsa_hemocomponente
    add column categoria_descarte varchar(40),
    add column data_descarte timestamptz;

alter table public.bolsa_hemocomponente
    add constraint ck_bolsa_categoria_descarte
        check (categoria_descarte is null
               or categoria_descarte in ('VALIDADE_EXPIRADA', 'TEMPERATURA_FORA_DO_PADRAO', 'BOLSA_DANIFICADA', 'OUTRO'));

-- Descartes feitos antes desta migration: sem motivo padronizado (OUTRO, mantendo o texto que
-- já havia) e com a última atualização da bolsa como data aproximada do descarte.
update public.bolsa_hemocomponente
set categoria_descarte = 'OUTRO',
    data_descarte = atualizado_em
where status = 'DESCARTADA'
  and categoria_descarte is null;
