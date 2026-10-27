"""
Checagens HTTP das campanhas e agendamentos de doação (PI3-174), contra a API rodando em localhost:8080.
Usa os usuários de teste de supabase/usuarios_de_teste.sql (médico, doador e admin), identificados pelo
header X-Usuario-Id. Cria o paciente "Paciente Teste PI3-174" e termina com a campanha encerrada;
a limpeza está em PI3-174_doacoes.md.

    python3 docs/evidencias/PI3-174/teste_doacoes.py

Os horários (QUINTA, SABADO, DOMINGO) precisam estar no futuro: ajuste-os antes de rodar de novo.
"""
import json
import sys
import urllib.error
import urllib.request

BASE = "http://localhost:8080/api/v1"
MEDICO = "abdd40a0-8e3d-4cf4-b4be-93d338368fd7"
DOADOR = "533c0abe-57f0-42ba-8304-fa69f7456578"
ADMIN = "8e7506af-602d-4ef4-85f4-533ec67ef113"
HOSPITAL = "barao-lucena"

falhas = 0


def chamar(metodo, caminho, usuario=None, corpo=None):
    dados = json.dumps(corpo).encode() if corpo is not None else None
    req = urllib.request.Request(BASE + caminho, data=dados, method=metodo)
    req.add_header("Content-Type", "application/json")
    if usuario:
        req.add_header("X-Usuario-Id", usuario)
    try:
        with urllib.request.urlopen(req) as r:
            texto = r.read().decode()
            return r.status, json.loads(texto) if texto else None
    except urllib.error.HTTPError as e:
        texto = e.read().decode()
        return e.code, json.loads(texto) if texto else None


def checar(nome, metodo, caminho, esperado, usuario=None, corpo=None):
    global falhas
    status, resp = chamar(metodo, caminho, usuario, corpo)
    ok = status == esperado
    falhas += 0 if ok else 1
    detalhe = resp.get("detail") if isinstance(resp, dict) and "detail" in resp else ""
    print(f"{'PASS' if ok else 'FAIL'} {status} (esperado {esperado}) {nome}" + (f" -> {detalhe}" if detalhe else ""))
    return resp


def igual(nome, obtido, esperado):
    global falhas
    ok = obtido == esperado
    falhas += 0 if ok else 1
    print(f"{'PASS' if ok else 'FAIL'} {nome}: {obtido!r}" + ("" if ok else f" (esperado {esperado!r})"))


campanha = {
    "hospitalId": HOSPITAL, "nome": "Paciente Teste PI3-174", "sexo": "FEMININO", "dataNascimento": "1990-05-10",
    "tipoSanguineo": "O_NEGATIVO", "tipoComponente": "HEMACIAS", "unidadesNecessarias": 2,
    "causa": "Teste automatizado", "gravidade": "CRITICO",
}
QUINTA = "2026-10-29T10:00:00-03:00"
SABADO = "2026-10-31T18:30:00-03:00"
DOMINGO = "2026-11-01T10:00:00-03:00"

checar("lista campanhas", "GET", "/campanhas", 200)
checar("status inválido na query", "GET", "/campanhas?status=XYZ", 400)
checar("campanha inexistente", "GET", "/campanhas/00000000-0000-0000-0000-000000000000", 404)
checar("publicar sem header", "POST", "/campanhas", 401, corpo=campanha)
checar("header que não é uuid", "POST", "/campanhas", 400, usuario="abc", corpo=campanha)
checar("doador publica", "POST", "/campanhas", 403, DOADOR, campanha)
checar("admin publica", "POST", "/campanhas", 403, ADMIN, campanha)
checar("campos vazios", "POST", "/campanhas", 400, MEDICO, {"nome": " "})
checar("hospital inexistente", "POST", "/campanhas", 400, MEDICO, {**campanha, "hospitalId": "BS-01"})
c = checar("médico publica", "POST", "/campanhas", 201, MEDICO, campanha)
cid = c["id"]
igual("campanha ativa", c["status"], "ATIVA")
igual("publicada por", c["publicadaPorNome"] is not None, True)
igual("idade calculada", c["paciente"]["idade"], 36)
igual("hospital", c["paciente"]["hospitalId"], HOSPITAL)
checar("consulta campanha", "GET", f"/campanhas/{cid}", 200)
e = checar("médico edita", "PUT", f"/campanhas/{cid}", 200, MEDICO, {**campanha, "unidadesNecessarias": 3, "gravidade": "ATENCAO"})
igual("unidades editadas", e["paciente"]["unidadesNecessarias"], 3)
igual("gravidade editada", e["paciente"]["gravidade"], "ATENCAO")
checar("doador edita", "PUT", f"/campanhas/{cid}", 403, DOADOR, campanha)

ag = f"/campanhas/{cid}/agendamentos"
checar("médico agenda", "POST", ag, 403, MEDICO, {"bancoId": "BS-01", "agendadoPara": QUINTA})
checar("agendar no domingo", "POST", ag, 422, DOADOR, {"bancoId": "BS-01", "agendadoPara": DOMINGO})
checar("agendar às 6h", "POST", ag, 422, DOADOR, {"bancoId": "BS-01", "agendadoPara": "2026-10-29T06:00:00-03:00"})
checar("agendar no passado", "POST", ag, 422, DOADOR, {"bancoId": "BS-01", "agendadoPara": "2026-10-20T10:00:00-03:00"})
checar("data mal formatada", "POST", ag, 400, DOADOR, {"bancoId": "BS-01", "agendadoPara": "29/10/2026"})
checar("banco inexistente", "POST", ag, 400, DOADOR, {"bancoId": "BS-99", "agendadoPara": QUINTA})
a = checar("doador agenda", "POST", ag, 201, DOADOR, {"bancoId": "BS-01", "agendadoPara": QUINTA})
aid = a["id"]
igual("status do agendamento", a["status"], "AGENDADO")
igual("paciente no agendamento", a["pacienteNome"], campanha["nome"])
checar("segundo agendamento em aberto", "POST", ag, 422, DOADOR, {"bancoId": "BS-01", "agendadoPara": SABADO})
lista = checar("lista campanhas", "GET", "/campanhas?status=ATIVA", 200)
igual("agendamentos em aberto na campanha", next(x for x in lista if x["id"] == cid)["agendamentosEmAberto"], 1)
meus = checar("meus agendamentos", "GET", "/agendamentos/meus", 200, DOADOR)
igual("quantidade dos meus", len(meus), 1)
checar("médico em /agendamentos/meus", "GET", "/agendamentos/meus", 403, MEDICO)
checar("doador vê agendamentos da campanha", "GET", ag, 403, DOADOR)
checar("admin vê agendamentos da campanha", "GET", ag, 200, ADMIN)
r = checar("doador remarca", "PATCH", f"/agendamentos/{aid}", 200, DOADOR, {"agendadoPara": SABADO})
igual("novo horário", r["agendadoPara"].startswith("2026-10-31T21:30") or r["agendadoPara"].startswith("2026-10-31T18:30"), True)
checar("médico remarca", "PATCH", f"/agendamentos/{aid}", 403, MEDICO, {"agendadoPara": QUINTA})
checar("doador marca realizado", "PATCH", f"/agendamentos/{aid}/status", 403, DOADOR, {"status": "REALIZADO"})
checar("falta antes do horário", "PATCH", f"/agendamentos/{aid}/status", 422, MEDICO, {"status": "FALTOU"})
checar("status AGENDADO", "PATCH", f"/agendamentos/{aid}/status", 400, MEDICO, {"status": "AGENDADO"})
checar("admin muda status", "PATCH", f"/agendamentos/{aid}/status", 403, ADMIN, {"status": "CANCELADO"})
s = checar("médico registra doação feita", "PATCH", f"/agendamentos/{aid}/status", 200, MEDICO, {"status": "REALIZADO"})
igual("status realizado", s["status"], "REALIZADO")
checar("alterar agendamento fechado", "PATCH", f"/agendamentos/{aid}/status", 422, MEDICO, {"status": "CANCELADO"})

a2 = checar("doador agenda de novo", "POST", ag, 201, DOADOR, {"bancoId": "BS-01", "agendadoPara": QUINTA})
checar("admin encerra", "PATCH", f"/campanhas/{cid}/encerramento", 403, ADMIN)
fim = checar("médico encerra", "PATCH", f"/campanhas/{cid}/encerramento", 200, MEDICO)
igual("campanha encerrada", fim["status"], "ENCERRADA")
igual("agendamentos em aberto depois de encerrar", fim["agendamentosEmAberto"], 0)
meus = checar("meus agendamentos", "GET", "/agendamentos/meus", 200, DOADOR)
igual("agendamento cancelado ao encerrar", next(x for x in meus if x["id"] == a2["id"])["status"], "CANCELADO")
checar("encerrar de novo", "PATCH", f"/campanhas/{cid}/encerramento", 422, MEDICO)
checar("editar encerrada", "PUT", f"/campanhas/{cid}", 422, MEDICO, campanha)
checar("agendar em encerrada", "POST", ag, 422, DOADOR, {"bancoId": "BS-01", "agendadoPara": QUINTA})

print(f"\n{falhas} falha(s)")
sys.exit(1 if falhas else 0)
