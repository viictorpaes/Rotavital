# 1. Estágio de build (Node.js 24 LTS — o Node 20 saiu de suporte em abril/2026)
FROM node:24-alpine AS build
WORKDIR /app

# npm ci instala exatamente o que está no package-lock.json (build reproduzível)
COPY package*.json ./
RUN npm ci

# tsc -b (checagem de tipos) + vite build: erro de tipo derruba o build da imagem
COPY . .
RUN npm run build

# 2. Estágio de produção: só o Nginx e os arquivos estáticos do build.
# Variante "unprivileged": o Nginx roda sem root, por isso escuta na 8080 (portas < 1024 exigem root).
FROM nginxinc/nginx-unprivileged:1.30-alpine

COPY --from=build /app/dist /usr/share/nginx/html

# A imagem oficial do Nginx troca ${PORT} e ${API_UPSTREAM} do template ao subir o container:
# no Compose a API é http://backend:8080; no Render, a URL pública do serviço da API.
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
ENV PORT=8080 \
    API_UPSTREAM=http://backend:8080

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
