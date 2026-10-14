#!/usr/bin/env bash
# ===================================================================
# Rota Vital — deploy (ou rollback) no Render a partir das imagens do GHCR.
#
# Usado pelos jobs "Deploy · Render" e "Rollback · Render" da esteira.
# Variáveis de ambiente:
#   VERSAO                  sha completo do commit = tag das imagens no GHCR
#   RENDER_DEPLOY_HOOK_API  deploy hook do serviço da API (segredo)
#   RENDER_DEPLOY_HOOK_WEB  deploy hook do serviço do frontend (segredo)
#   API_URL, WEB_URL        URLs públicas dos dois serviços (variáveis do repositório)
# ===================================================================
set -euo pipefail

: "${VERSAO:?informe o sha da versão}"
: "${RENDER_DEPLOY_HOOK_API:?configure o segredo RENDER_DEPLOY_HOOK_API no environment producao}"
: "${RENDER_DEPLOY_HOOK_WEB:?configure o segredo RENDER_DEPLOY_HOOK_WEB no environment producao}"
: "${API_URL:?configure a variável API_URL (ex.: https://rotavital-api.onrender.com)}"
: "${WEB_URL:?configure a variável WEB_URL (ex.: https://rotavital-web.onrender.com)}"

# A versão vem de um campo digitado no rollback: só aceita um sha completo, que é a tag
# das imagens no GHCR (e evita injeção na URL do hook).
if [[ ! "$VERSAO" =~ ^[0-9a-f]{40}$ ]]
then
    echo "::error::Versão inválida: '$VERSAO'. Use o sha completo do commit (40 caracteres, ex.: git log --format=%H)."
    exit 1
fi

dono=$(echo "${GITHUB_REPOSITORY_OWNER:?}" | tr "[:upper:]" "[:lower:]")
imagem_api="ghcr.io/$dono/rotavital-backend:$VERSAO"
imagem_web="ghcr.io/$dono/rotavital-frontend:$VERSAO"

# O deploy hook do Render aceita a imagem a implantar no parâmetro imgURL (URL-encoded).
implantar()
{
    local hook="$1" imagem="$2" codificada
    codificada=$(python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1], safe=""))' "$imagem")
    curl --fail --silent --show-error --output /dev/null -X POST "${hook}&imgURL=${codificada}"
    echo "Render: deploy de $imagem solicitado"
}

implantar "$RENDER_DEPLOY_HOOK_API" "$imagem_api"
implantar "$RENDER_DEPLOY_HOOK_WEB" "$imagem_web"

# Smoke test: só termina verde quando os dois serviços respondem com a versão nova
# e o /actuator/health da API está UP (o que inclui a conexão com o banco).
# Até 15 minutos: o Render baixa a imagem e o plano gratuito tem "cold start".
api_ok=false
web_ok=false
for tentativa in $(seq 1 90)
do
    if [ "$api_ok" = false ]
    then
        info=$(curl --silent --max-time 20 "$API_URL/actuator/info" || true)
        saude=$(curl --silent --max-time 20 "$API_URL/actuator/health" || true)
        if grep -q "\"versao\":\"$VERSAO\"" <<< "$info" && grep -q '"status":"UP"' <<< "$saude"
        then
            api_ok=true
            echo "✅ API no ar com a versão $VERSAO: $API_URL"
        fi
    fi

    if [ "$web_ok" = false ]
    then
        if curl --silent --max-time 20 "$WEB_URL/versao.json" | grep -q "\"versao\":\"$VERSAO\""
        then
            web_ok=true
            echo "✅ Frontend no ar com a versão $VERSAO: $WEB_URL"
        fi
    fi

    if [ "$api_ok" = true ] && [ "$web_ok" = true ]
    then
        # O frontend chega na API pelo proxy do Nginx (mesmo caminho que o navegador usa).
        curl --fail --silent --show-error --max-time 60 --output /dev/null "$WEB_URL/api/v1/pontos"
        echo "✅ Frontend → API → banco respondendo em $WEB_URL/api/v1/pontos"
        {
            echo "### 🚀 Versão \`$VERSAO\` no ar"
            echo ""
            echo "| Serviço | URL | Imagem |"
            echo "| :--- | :--- | :--- |"
            echo "| Frontend | $WEB_URL | \`$imagem_web\` |"
            echo "| API | $API_URL/actuator/health | \`$imagem_api\` |"
        } >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
        exit 0
    fi

    echo "Aguardando o Render publicar $VERSAO (tentativa $tentativa/90)..."
    sleep 10
done

echo "::error::A versão $VERSAO não respondeu em 15 minutos (API: $api_ok, frontend: $web_ok). Veja os logs do deploy no painel do Render."
exit 1
