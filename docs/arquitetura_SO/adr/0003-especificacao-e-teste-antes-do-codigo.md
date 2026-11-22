# ADR 0003: Especificação e teste antes do código (SDD + TDD)

Status: aceito · 24/11/2026

## Contexto

O backend tem 430 testes unitários e 77,5 % das linhas cobertas (JaCoCo, 22/11/2026), mas a cobertura não é medida na
esteira e pode cair sem ninguém ver. O frontend não tem nenhum teste. A refatoração da Parte 3 mexe no coração do
Estoque em uma semana, com oito pessoas e ajuda de ferramentas de IA, e a devolutiva de 17/11 perguntou pela cobertura
atual e planejada e sugeriu SDD.

## Decisão

Toda história da Parte 3 começa por uma pasta em `docs/specs/` (`spec.md`, `plan.md`, `tasks.md`), revisada no PR antes
do código. Cada tarefa é feita em TDD (teste vermelho → código mínimo → refatoração). O JaCoCo entra no `pom.xml` e quebra
o build abaixo de 95 % de linhas no domínio do Estoque e de 80 % no backend total.

## Consequências

- **Melhora:** as invariantes ficam escritas antes de existir a classe, então o domínio nasce rico. O plano de cada
  história já diz em que camada cada peça fica, e a IA recebe uma especificação, não um pedido solto. A cobertura deixa
  de depender de disciplina individual.
- **Piora:** cada história leva mais tempo no começo. Metas de cobertura podem incentivar teste que só passa por linhas
  sem conferir nada, por isso a revisão do PR olha os critérios de aceite, não só o número.
- **Alternativa descartada:** medir a cobertura só no fim do semestre. Seria mais simples, mas a refatoração quebraria
  comportamento sem aviso.
