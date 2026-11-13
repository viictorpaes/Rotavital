# Postgres 17 (mesma major do Supabase). Na primeira subida, o entrypoint aplica
# as migrations montadas pelo Compose e carrega a seed local empacotada na imagem.
FROM postgres:17-alpine

COPY --chmod=755 00_rotavital.sh /docker-entrypoint-initdb.d/00_rotavital.sh
COPY --chmod=644 01_seed_local.sql /rotavital/01_seed_local.sql
RUN sed -i 's/\r$//' /docker-entrypoint-initdb.d/00_rotavital.sh
