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
# aponta usuario.auth_user_id para auth.users. Aqui criamos só o mínimo para a FK.
psql_rotavital <<'SQL'
create schema if not exists auth;
create table if not exists auth.users (id uuid primary key);
SQL

for migration in /rotavital/migrations/*.sql
do
    echo "Rota Vital: aplicando $(basename "$migration")"
    psql_rotavital -f "$migration"
done

# O seed é opcional: os testes de integração não montam o arquivo e criam os próprios dados.
if [ -f /rotavital/seed.sql ]
then
    echo "Rota Vital: carregando seed.sql"
    psql_rotavital -f /rotavital/seed.sql
fi