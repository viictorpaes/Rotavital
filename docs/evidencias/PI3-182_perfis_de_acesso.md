<h1 align="center">Evidências · PI3-182 · Perfis de acesso <br>
<img src="https://img.shields.io/badge/-Playwright-111827?style=flat-square&logo=playwright&logoColor=2EAD33"/>
<img src="https://img.shields.io/badge/-React-111827?style=flat-square&logo=react&logoColor=61DAFB"/></h1>

> Navegação real no sistema rodando localmente (frontend Vite na porta `5173` e backend Spring Boot na porta
> `8080`, conectado ao Supabase), feita em **10/10/2026** a partir do código-base `ab778c5` com um navegador
> automatizado (Playwright · Chromium headless · janela 1440×900). Cada cenário começa numa aba nova, sem
> sessão anterior. Regra de referência: [`PERFIS_DE_ACESSO.md`, seção 4](../PERFIS_DE_ACESSO.md#4-telas).

**Resultado:** 9/9 cenários terminaram na rota esperada.

| # | Perfil | Cenário | Rota esperada | Rota obtida | Menu lateral | |
|---|---|---|---|---|---|---|
| 1 | Sem login | Abre `/painel` direto | `/login` | `/login` | — | ✅ |
| 2 | Médico | Entra pelo login | `/painel` | `/painel` | Início, Estoque, Requisição, Rede, Pacientes, Doações | ✅ |
| 3 | Médico | Tenta abrir `/admin` | `/painel` | `/painel` | Início, Estoque, Requisição, Rede, Pacientes, Doações | ✅ |
| 4 | Médico | Tenta abrir `/portal-doador` | `/login` ¹ | `/login` | — | ✅ |
| 5 | Doador | Entra pelo login | `/portal-doador` | `/portal-doador` | — (portal sem menu) | ✅ |
| 6 | Doador | Tenta abrir `/estoque` | `/portal-doador` | `/portal-doador` | — | ✅ |
| 7 | Doador | Tenta abrir `/admin` | `/portal-doador` | `/portal-doador` | — | ✅ |
| 8 | Admin | Entra pelo login | `/admin` | `/admin` | Início, Estoque, Requisição, Rede, Pacientes, Doações, **Diagnóstico & Logs** | ✅ ² |
| 9 | Admin | Abre `/estoque` | `/estoque` | `/estoque` | Início, Estoque, Requisição, Rede, Pacientes, Doações, **Diagnóstico & Logs** | ✅ |

¹ Comportamento de hoje, registrado na nota da [seção 4](../PERFIS_DE_ACESSO.md#4-telas): o médico logado é
mandado para o login em vez da tela inicial dele. O ajuste está previsto na PI3-184.

² A tela de diagnóstico abriu, mas o console registrou 2 respostas `500` de `GET /api/v1/diagnostico`. O erro
já existia e não tem relação com perfis: o `DiagnosticoController` usa `@CrossOrigin(origins = "*")` e o
`CorsConfig` global liga `allowCredentials(true)`, combinação que o Spring recusa. Só o campo "Uptime" fica
vazio.

## Como reproduzir à mão

```bash
# terminal 1 — backend (precisa do backend/.env com SUPABASE_DB_PASSWORD)
cd backend && mvn spring-boot:run

# terminal 2 — frontend
cd frontend && npm run dev
```

Abra http://localhost:5173 e repita os cenários da tabela:

- **Médico:** clique no card "Médico", digite um nome e clique em "Entrar".
- **Doador:** clique no card "Doador", digite um nome e clique em "Entrar".
- **Admin:** digite `admin` no nome e clique em "Entrar como Admin →".
- **Rotas proibidas:** com o perfil logado, digite a rota na barra de endereço.
- **Trocar de perfil:** use "Sair" ou abra uma janela anônima.

## Prints

### 1. Sem login · abre `/painel` → vai para `/login`
<img src="PI3-182/01_sem_login_abre_painel.png" width="720"/>

### 2. Médico · entra → `/painel`, com o menu operacional
<img src="PI3-182/02_medico_entra.png" width="720"/>

### 3. Médico · tenta abrir `/admin` → volta para `/painel`
<img src="PI3-182/03_medico_tenta_abrir_admin.png" width="720"/>

### 4. Médico · tenta abrir `/portal-doador` → vai para `/login` ¹
<img src="PI3-182/04_medico_tenta_abrir_portal_doador.png" width="720"/>

### 5. Doador · entra → `/portal-doador`, sem o menu operacional
<img src="PI3-182/05_doador_entra.png" width="720"/>

### 6. Doador · tenta abrir `/estoque` → volta para `/portal-doador`
<img src="PI3-182/06_doador_tenta_abrir_estoque.png" width="720"/>

### 7. Doador · tenta abrir `/admin` → volta para `/portal-doador`
<img src="PI3-182/07_doador_tenta_abrir_admin.png" width="720"/>

### 8. Admin · entra → `/admin`, com "Diagnóstico & Logs" no menu
<img src="PI3-182/08_admin_entra.png" width="720"/>

### 9. Admin · abre `/estoque` → permitido, com os dados do Supabase
<img src="PI3-182/09_admin_abre_estoque.png" width="720"/>
