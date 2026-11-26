#!/bin/sh
# Cria no Grafana o usuário de leitura (papel Viewer) do professor, com as credenciais do .env.
# Roda uma vez a cada "up"; se o usuário já existir, o Grafana responde 412 e nada muda.
set -u
GRAFANA=http://grafana:3000

echo "Esperando o Grafana responder..."
until curl -fsS "$GRAFANA/api/health" > /dev/null 2>&1; do sleep 2; done

resposta=$(curl -s -o /dev/null -w "%{http_code}" -u "$GF_ADMIN_USER:$GF_ADMIN_PASSWORD" \
  -H "Content-Type: application/json" -X POST "$GRAFANA/api/admin/users" \
  -d "{\"name\":\"Professor (leitura)\",\"login\":\"$VIEWER_USER\",\"password\":\"$VIEWER_PASSWORD\",\"OrgId\":1}")

case "$resposta" in
  200) echo "Usuário $VIEWER_USER criado com papel Viewer." ;;
  412) echo "Usuário $VIEWER_USER já existia." ;;
  *)   echo "Não consegui criar o usuário $VIEWER_USER (HTTP $resposta)." ; exit 1 ;;
esac
