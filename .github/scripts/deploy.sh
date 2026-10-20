#!/usr/bin/env bash
# ===================================================================
# Rota Vital — deploy (ou rollback): API no Render, site na Vercel.
#
# Usado pelos jobs "Deploy · Render + Vercel" e "Rollback · Render + Vercel" da esteira.
# Variáveis de ambiente:
#   VERSAO                  sha completo do commit = tag da imagem da API no GHCR
#   RENDER_DEPLOY_HOOK_API  deploy hook do serviço da API no Render (segredo)
#   VERCEL_TOKEN            token de acesso da Vercel (segredo)
#   VERCEL_ORG_ID           id da conta/time na Vercel (segredo)
#   VERCEL_PROJECT_ID       id do projeto do site na Vercel (segredo)
#   API_URL, WEB_URL        URLs públicas da API e do site (variáveis do repositório)
# ===================================================================
set -euo pipefail

: "${VERSAO:?informe o sha da versão}"
: "${RENDER_DEPLOY_HOOK_API:?configure o segredo RENDER_DEPLOY_HOOK_API no environment producao}"
: "${VERCEL_TOKEN:?configure o segredo VERCEL_TOKEN no environment producao}"
: "${VERCEL_ORG_ID:?configure o segredo VERCEL_ORG_ID no environment producao}"
: "${VERCEL_PROJECT_ID:?configure o segredo VERCEL_PROJECT_ID no environment producao}"
: "${API_URL:?configure a variável API_URL (ex.: https://rotavital-api.onrender.com)}"
: "${WEB_URL:?configure a variável WEB_URL (ex.: https://rotavital-web.vercel.app)}"

# A versão vem de um campo digitado no rollback: só aceita um sha completo, que é a tag
# da imagem no GHCR (e evita injeção na URL do hook e nos comandos do git).
if [[ ! "$VERSAO" =~ ^[0-9a-f]{40}$ ]]
then
    echo "::error::Versão inválida: '$VERSAO'. Use o sha completo do commit (40 caracteres, ex.: git log --format=%H)."
    exit 1
fi

# CLI da Vercel com versão fixa: uma versão nova da CLI não muda o deploy sem passar por um commit.
VERCEL_CLI=vercel@64.0.2

dono=$(echo "${GITHUB_REPOSITORY_OWNER:?}" | tr "[:upper:]" "[:lower:]")
imagem_api="ghcr.io/$dono/rotavital-backend:$VERSAO"

# 1. API no Render: o deploy hook aceita a imagem a implantar no parâmetro imgURL (URL-encoded).
# Vem primeiro porque o Render leva alguns minutos baixando a imagem; o site sobe enquanto isso.
codificada=$(python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1], safe=""))' "$imagem_api")
curl --fail --silent --show-error --output /dev/null -X POST "${RENDER_DEPLOY_HOOK_API}&imgURL=${codificada}"
echo "Render: deploy de $imagem_api solicitado"

# 2. Site na Vercel, construído a partir do código da versão.
# No deploy, é o próprio checkout. No rollback, é o código do sha pedido (git worktree), com o
# frontend/vercel.json atual: o de uma versão antiga pode não existir ou apontar para outra API.
frontend="$PWD/frontend"
if [ "$(git rev-parse HEAD)" != "$VERSAO" ]
then
    codigo="${RUNNER_TEMP:-$(mktemp -d)}/rotavital-$VERSAO"
    git fetch --quiet --depth 1 origin "$VERSAO"
    git worktree add --quiet --detach "$codigo" "$VERSAO"
    cp frontend/vercel.json "$codigo/frontend/vercel.json"
    frontend="$codigo/frontend"
fi

# O smoke test confere a versão do site por /versao.json (o Vite copia public/ para o build).
mkdir -p "$frontend/public"
printf '{"versao":"%s"}\n' "$VERSAO" > "$frontend/public/versao.json"

# pull: configurações do projeto; build: npm ci + npm run build (frontend/vercel.json);
# deploy --prebuilt --prod: publica o build no domínio de produção e imprime a URL do deploy.
vercel()
{
    npx --yes "$VERCEL_CLI" "$@" --token "$VERCEL_TOKEN"
}
cd "$frontend"
vercel pull --yes --environment=production
vercel build --prod
deploy_web=$(vercel deploy --prebuilt --prod)
cd - > /dev/null
echo "Vercel: deploy $deploy_web publicado em produção"

# Smoke test: só termina verde quando a API e o site respondem com a versão nova
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
            echo "✅ Site no ar com a versão $VERSAO: $WEB_URL"
        fi
    fi

    if [ "$api_ok" = true ] && [ "$web_ok" = true ]
    then
        # O site chega na API pelo rewrite /api/ da Vercel (mesmo caminho que o navegador usa).
        curl --fail --silent --show-error --max-time 60 --output /dev/null "$WEB_URL/api/v1/pontos"
        echo "✅ Site → API → banco respondendo em $WEB_URL/api/v1/pontos"
        {
            echo "### 🚀 Versão \`$VERSAO\` no ar"
            echo ""
            echo "| Serviço | URL | Origem |"
            echo "| :--- | :--- | :--- |"
            echo "| Site (Vercel) | $WEB_URL | $deploy_web |"
            echo "| API (Render) | $API_URL/actuator/health | \`$imagem_api\` |"
        } >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
        exit 0
    fi

    echo "Aguardando a versão $VERSAO no ar (tentativa $tentativa/90)..."
    sleep 10
done

echo "::error::A versão $VERSAO não respondeu em 15 minutos (API: $api_ok, site: $web_ok). Veja os logs do deploy no painel do Render (API) e da Vercel (site)."
exit 1
