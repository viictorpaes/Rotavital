#!/bin/sh
# Gerador de carga do Rota Vital: mantém os gráficos com dados (métricas, logs e traces).
# Chama a API pela rede interna do Docker, em ciclos, misturando sucessos e erros de propósito.
set -u
API=http://backend:8080/api/v1

echo "Esperando o backend responder..."
until curl -fsS "$API/pontos" > /dev/null 2>&1; do sleep 3; done
echo "Gerando carga em $API"

i=0
while true; do
  i=$((i + 1))

  # Consultas normais (cada uma vira um trace com spans de banco)
  curl -s -o /dev/null "$API/pontos"
  curl -s -o /dev/null "$API/conexoes"
  curl -s -o /dev/null "$API/bancos/BS-01/estoque"
  curl -s -o /dev/null "$API/bancos/BS-01/estoque/resumo"
  curl -s -o /dev/null "$API/bancos/BS-01/alertas"
  curl -s -o /dev/null "$API/hemocentros"
  curl -s -o /dev/null "$API/campanhas"

  # Métricas de negócio: cálculo de rota (timer) e sugestão de lote (counter)
  for destino in HOSP-01 HOSP-02 HOSP-03 HOSP-04 HOSP-06; do
    curl -s -o /dev/null "$API/rotas?origemId=BS-01&destinoId=$destino"
  done
  curl -s -o /dev/null "$API/rotas?origemId=BS-02&destinoId=HOSP-05"
  curl -s -o /dev/null "$API/bancos/BS-01/sugestao-lote?tipoComponente=HEMACIAS&tipoSanguineo=O_NEGATIVO&quantidade=2"
  curl -s -o /dev/null "$API/bancos/BS-01/sugestao-lote?tipoComponente=PLAQUETAS&tipoSanguineo=A_POSITIVO&quantidade=30"

  # Métrica de negócio rotavital.bolsas.cadastradas: uma bolsa nova no 1º ciclo e a cada 60 (uns 3 min),
  # alternando o componente. Desligado quando CARGA_SOMENTE_LEITURA=true (modo Supabase na nuvem).
  if [ "${CARGA_SOMENTE_LEITURA:-false}" != "true" ] && { [ "$i" -eq 1 ] || [ $((i % 60)) -eq 0 ]; }; then
    n=$((i / 60 % 4))
    case $n in
      0) comp=HEMACIAS;        temp=4.0 ;;
      1) comp=PLASMA;          temp=-25.0 ;;
      2) comp=PLAQUETAS;       temp=22.0 ;;
      *) comp=CRIOPRECIPITADO; temp=-25.0 ;;
    esac
    agora=$(date +%s)
    coleta=$(date -u -d "@$((agora - 86400))" +%Y-%m-%d)
    validade=$(date -u -d "@$((agora + 4 * 86400))" +%Y-%m-%d)
    curl -s -o /dev/null -X POST "$API/hemocomponentes" -H "Content-Type: application/json" \
      -d "{\"tipoComponente\":\"$comp\",\"tipoSanguineo\":\"O_POSITIVO\",\"dataColeta\":\"$coleta\",\"dataValidade\":\"$validade\",\"loteSintetico\":\"CARGA-$agora\",\"volumeMl\":300,\"temperaturaCelsius\":$temp,\"bancoOrigemId\":\"BS-01\"}"
  fi

  # Casos de erro (a cada 3 ciclos): 400, 404 e 404 de rota inexistente
  if [ $((i % 3)) -eq 0 ]; then
    curl -s -o /dev/null "$API/rotas"
    curl -s -o /dev/null "$API/rotas?origemId=XX-99&destinoId=HOSP-01"
    curl -s -o /dev/null "$API/hemocomponentes/NAO-EXISTE"
    curl -s -o /dev/null "$API/bancos/BS-01/sugestao-lote?tipoComponente=HEMACIAS&tipoSanguineo=O_NEGATIVO&quantidade=0"
  fi

  sleep 2
done
