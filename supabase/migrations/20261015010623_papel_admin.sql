-- ===================================================================
-- Rota Vital — Perfil ADMIN em usuario.papel (PI3-183).
-- Decisão 3 da PI3-182 (docs/PERFIS_DE_ACESSO.md): o admin fica na mesma tabela
-- usuario e entra do mesmo jeito que médico e doador (Supabase Auth).
-- As FKs compostas (id, papel) de campanha, agendamento e procedimento continuam
-- exigindo MEDICO ou DOADOR, então o admin não grava ações clínicas (decisão 2).
-- ===================================================================

alter table public.usuario
    drop constraint ck_usuario_papel,
    add constraint ck_usuario_papel check (papel in ('MEDICO', 'DOADOR', 'ADMIN'));
