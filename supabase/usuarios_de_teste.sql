-- ===================================================================
-- Rota Vital — Usuários de teste dos três perfis (PI3-183).
--
-- 1. No Supabase: Authentication → Users → Add user → Create new user, com
--    "Auto Confirm User" marcado, para cada e-mail abaixo. A senha é escolhida
--    na hora e combinada com o time fora do Git (o hash fica em auth.users).
-- 2. Depois rode este script (SQL Editor). Ele liga cada login a uma linha de
--    public.usuario com o papel. Pode rodar de novo: quem já está ligado é pulado.
--
-- Precisa da migration 20261015010623_papel_admin.sql (papel ADMIN).
-- ===================================================================

insert into public.usuario (auth_user_id, nome, papel, ponto_rede_id, tipo_sanguineo)
select u.id, dados.nome, dados.papel, dados.ponto_rede_id, dados.tipo_sanguineo
from (values
        ('medico@rotavital.test', 'Dra. Ana Lima',  'MEDICO', 'HOSP-01', null),
        ('doador@rotavital.test', 'Carlos Souza',   'DOADOR', null,      'O_NEGATIVO'),
        ('admin@rotavital.test',  'Equipe Técnica', 'ADMIN',  null,      null)
     ) as dados (email, nome, papel, ponto_rede_id, tipo_sanguineo)
join auth.users u on lower(u.email) = dados.email
on conflict (auth_user_id) do nothing;
