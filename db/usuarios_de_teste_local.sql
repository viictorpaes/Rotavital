-- ===================================================================
-- Rota Vital — Contas do Supabase Auth dos usuários de teste, no Postgres local (Compose).
--
-- O login confere a senha no Supabase Auth (na nuvem), que devolve o id da conta.
-- O backend acha o perfil pela linha de usuario com esse auth_user_id; por isso os
-- ids abaixo são os mesmos de auth.users no projeto do Supabase. Depois deste arquivo,
-- o db/00_rotavital.sh roda supabase/usuarios_de_teste.sql, que cria as linhas de usuario.
--
-- As senhas não ficam aqui: são combinadas com o time fora do Git.
-- ===================================================================

insert into auth.users (id, email)
values ('a44dc689-43b3-40c9-87e9-0b7d9f600911', 'medico@rotavital.test'),
       ('4572c88d-37ed-474f-a4ab-4049adeeea2c', 'doador@rotavital.test'),
       ('c98cfae9-4f4d-4225-b8a8-fe9cdd81c214', 'admin@rotavital.test')
on conflict (id) do nothing;
