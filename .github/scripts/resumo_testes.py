"""Resumo dos testes do backend em Markdown, para o resumo da execução no GitHub Actions.

Uso: python3 resumo_testes.py backend/target >> "$GITHUB_STEP_SUMMARY"

Lê os XML do surefire (unitários) e do failsafe (integração e aceitação) e
escreve uma tabela com o total de cada nível.
"""
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def somar(pasta: Path) -> dict:
    total = {"testes": 0, "falhas": 0, "erros": 0, "ignorados": 0}
    for arquivo in sorted(pasta.glob("TEST-*.xml")):
        suite = ET.parse(arquivo).getroot()
        total["testes"] += int(suite.get("tests", 0))
        total["falhas"] += int(suite.get("failures", 0))
        total["erros"] += int(suite.get("errors", 0))
        total["ignorados"] += int(suite.get("skipped", 0))
    return total


def main() -> None:
    target = Path(sys.argv[1] if len(sys.argv) > 1 else "backend/target")
    niveis = [
        ("Unitários (surefire)", somar(target / "surefire-reports")),
        ("Integração + aceitação (failsafe)", somar(target / "failsafe-reports")),
    ]

    vermelho = any(n["falhas"] or n["erros"] for _, n in niveis)
    print(f"### {'🔴 Testes falharam' if vermelho else '🟢 Testes passaram'}")
    print()
    print("| Nível | Testes | Falhas | Erros | Ignorados |")
    print("| :--- | ---: | ---: | ---: | ---: |")
    for nome, n in niveis:
        print(f"| {nome} | {n['testes']} | {n['falhas']} | {n['erros']} | {n['ignorados']} |")


if __name__ == "__main__":
    main()