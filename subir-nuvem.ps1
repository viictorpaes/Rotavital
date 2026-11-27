param(
    [switch]$SemBuild,
    [switch]$PermitirCadastroCarga
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host " Rota Vital - Subindo Aplicacao + Observabilidade (SUPABASE CLOUD)" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# 1. Validar .env
if (-not (Test-Path ".env")) {
    throw "Arquivo .env nao encontrado em $PSScriptRoot. Copie de .env.example e preencha as chaves."
}

$envMap = @{}
Get-Content ".env" | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
        $idx = $line.IndexOf("=")
        $envMap[$line.Substring(0, $idx).Trim()] = $line.Substring($idx + 1).Trim()
    }
}

foreach ($reqKey in @("SUPABASE_DB_PASSWORD", "GRAFANA_ADMIN_PASSWORD", "GRAFANA_VIEWER_PASSWORD")) {
    if ([string]::IsNullOrWhiteSpace($envMap[$reqKey])) {
        throw "Variavel obrigatoria '$reqKey' esta vazia ou ausente no arquivo .env!"
    }
}

# 2. Por padrao, evita que o gerador de carga grave bolsas sinteticas CARGA-* no Supabase compartilhado
if ($PermitirCadastroCarga) {
    $env:CARGA_SOMENTE_LEITURA = "false"
    Write-Host "[Info] Gerador de carga com cadastro de bolsas HABILITADO no Supabase." -ForegroundColor Yellow
} else {
    $env:CARGA_SOMENTE_LEITURA = "true"
    Write-Host "[Info] Gerador de carga em modo SOMENTE LEITURA (protege o banco Supabase de bolsas sinteticas)." -ForegroundColor Green
}

# 3. Remover conteineres antigos parados que possam causar conflito de nome e parar banco local se estiver ligado
Write-Host "[1/4] Limpando conteineres parados e desligando Postgres local (db)..." -ForegroundColor Gray
$ErrorActionPreference = "Continue"
docker stop rotavital-db-1 *>$null
docker rm rotavital-db-1 *>$null
docker ps -a --filter "name=rotavital-" --filter "status=exited" -q | ForEach-Object { if ($_) { docker rm $_ *>$null } }
docker ps -a --filter "name=rotavital-" --filter "status=created" -q | ForEach-Object { if ($_) { docker rm $_ *>$null } }
$ErrorActionPreference = "Stop"

# 4. Subir toda a stack apontando para o Supabase na nuvem
Write-Host "[2/4] Subindo backend (Supabase), frontend e stack completa de observabilidade..." -ForegroundColor Gray
$composeArgs = @("-f", "compose.yaml", "-f", "compose.supabase.yaml", "-f", "compose.observabilidade.yaml", "up", "-d", "--remove-orphans")
if (-not $SemBuild) {
    $composeArgs += "--build"
}
$ErrorActionPreference = "Continue"
& docker compose @composeArgs
$composeExit = $LASTEXITCODE
$ErrorActionPreference = "Stop"
if ($composeExit -ne 0) {
    throw "Falha ao executar docker compose up (codigo $composeExit)."
}

# 5. Confirmar saude do backend conectado ao Supabase e exibir URLs
Write-Host "[3/4] Verificando conexao do backend com o Supabase..." -ForegroundColor Gray
$dbUrl = docker inspect rotavital-backend-1 --format "{{range .Config.Env}}{{println .}}{{end}}" | Select-String "^SPRING_DATASOURCE_URL="
Write-Host "      $dbUrl" -ForegroundColor DarkGray

Write-Host "[4/4] Status dos servicos:" -ForegroundColor Gray
docker compose -f compose.yaml -f compose.supabase.yaml -f compose.observabilidade.yaml ps --format "table {{.Name}}`t{{.Status}}`t{{.Ports}}"

$tunnelUrl = "(aguardando geracao - rode: npm run obs:cloud:url)"
$ErrorActionPreference = "Continue"
for ($t = 0; $t -lt 10; $t++) {
    $cfLogs = (docker logs rotavital-cloudflared-1 2>&1) -join "`n"
    $urlMatch = [regex]::Match($cfLogs, "https://[a-z0-9-]+\.trycloudflare\.com")
    if ($urlMatch.Success) {
        $tunnelUrl = $urlMatch.Value
        break
    }
    Start-Sleep -Seconds 2
}
$ErrorActionPreference = "Stop"

$webPort = if ($envMap["WEB_PORT"]) { $envMap["WEB_PORT"] } else { "8080" }
$apiPort = if ($envMap["API_PORT"]) { $envMap["API_PORT"] } else { "8082" }
$gfPort  = if ($envMap["GRAFANA_PORT"]) { $envMap["GRAFANA_PORT"] } else { "3000" }
$pmPort  = if ($envMap["PROMETHEUS_PORT"]) { $envMap["PROMETHEUS_PORT"] } else { "9090" }

Write-Host "`n=================================================================" -ForegroundColor Green
Write-Host " AMBIENTE EM NUVEM (SUPABASE) + OBSERVABILIDADE NO AR!" -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Green
Write-Host " Frontend (App Web) : http://localhost:$webPort"
Write-Host " Backend API direta : http://localhost:$apiPort/api/v1/pontos"
Write-Host " Grafana (Local)    : http://localhost:$gfPort"
Write-Host " Prometheus (Local) : http://localhost:$pmPort/targets"
Write-Host " Grafana (Publico)  : $tunnelUrl" -ForegroundColor Yellow
Write-Host "=================================================================" -ForegroundColor Green
