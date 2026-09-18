# 1. Estagio de build (Node.js 20)
FROM node:20-alpine AS build
WORKDIR /app

# Copia dependencias e instala
COPY package*.json ./
RUN npm install

# Copia o codigo e compila
COPY . .
RUN npm run build

# 2. Estagio de producao com Nginx
FROM nginx:alpine

# Copia os arquivos compilados do React/Vite
COPY --from=build /app/dist /usr/share/nginx/html

# Copia a configuracao do Nginx com suporte a rotas do React Router
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

