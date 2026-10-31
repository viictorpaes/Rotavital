#!/bin/sh
# ===================================================================
# Rota Vital — cria o schema num Postgres "puro" (Compose e testes de integração).
#
# Roda uma única vez, na primeira subida do container com o volume vazio
# (é o comportamento do /docker-entrypoint-initdb.d da imagem oficial do Postgres).
# A fonte de verdade do schema continua sendo supabase/migrations/: este script
# só aplica as mesmas migrations, na ordem do prefixo de data, e depois o seed.
#
# Vai para dentro da imagem (db/Dockerfile) em vez de ser montado da máquina:
# o Docker Desktop do macOS não deixa executar scripts de um bind mount.
# ===================================================================
set -e

psql_rotavital()
{
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" "$@"
}

# No Supabase o schema auth (Supabase Auth) já existe; a migration escopo_clinico
# aponta usuario.auth_user_id para auth.users, e a perfil_doador copia o e-mail de lá.
# Aqui criamos só o mínimo para as duas funcionarem.
psql_rotavital <<'SQL'
create schema if not exists auth;
create table if not exists auth.users (id uuid primary key, email text);
SQL

for migration in /rotavital/migrations/*.sql
do
    echo "Rota Vital: aplicando $(basename "$migration")"
    psql_rotavital -f "$migration"
done

# A senha continua sendo conferida no Supabase Auth (na nuvem), mas as contas criadas lá
# não aparecem no auth.users daqui. Sem esta FK, o cadastro de doador pelo portal grava
# a linha em usuario mesmo no Postgres local. No Supabase a FK continua valendo.
psql_rotavital -c "alter table public.usuario drop constraint fk_usuario_auth_user;"

# O seed é opcional: os testes de integração não montam o arquivo e criam os próprios dados.
if [ -f /rotavital/seed.sql ]
then
    echo "Rota Vital: carregando seed.sql"
    psql_rotavital -f /rotavital/seed.sql
fi

# Usuários de teste (médico, doador e admin), ligados às contas do Supabase Auth: com eles
# o login funciona no Compose. Também é opcional, e os testes de integração não usam.
if [ -f /rotavital/usuarios_de_teste_local.sql ]
then
    echo "Rota Vital: ligando os usuários de teste"
    psql_rotavital -f /rotavital/usuarios_de_teste_local.sql
    psql_rotavital -f /rotavital/usuarios_de_teste.sql
fi
