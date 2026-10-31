# Postgres 17 (mesma major do Supabase) com o script que, na primeira subida,
# aplica as migrations de supabase/migrations e o seed (montados pelo compose.yaml).
FROM postgres:17-alpine

COPY --chmod=755 00_rotavital.sh /docker-entrypoint-initdb.d/00_rotavital.sh
RUN sed -i 's/\r$//' /docker-entrypoint-initdb.d/00_rotavital.sh
