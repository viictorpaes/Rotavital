-- ===================================================================
-- Rota Vital — Cadastro e perfil do doador (PI3-203).
-- O doador se cadastra pelo portal e consulta os próprios dados. A senha continua
-- só no Supabase Auth (auth.users); aqui ficam os dados que o portal mostra.
-- As três colunas são opcionais: médico e admin não as usam, e os usuários
-- cadastrados antes desta migration continuam válidos.
-- ===================================================================

alter table public.usuario
    add column email           text,
    add column telefone        text,
    add column data_nascimento date,
    -- O mesmo e-mail do login; o Supabase Auth já garante que é único lá, aqui evita duas linhas para ele.
    add constraint ck_usuario_email_formato check (email is null or email ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    -- Só dígitos, com DDD: 10 (fixo) ou 11 (celular).
    add constraint ck_usuario_telefone_formato check (telefone is null or telefone ~ '^[0-9]{10,11}$');

create unique index uq_usuario_email on public.usuario (lower(email));

-- Quem já estava ligado ao Supabase Auth ganha o e-mail do login.
update public.usuario u
set email = lower(a.email)
from auth.users a
where a.id = u.auth_user_id
  and u.email is null;
