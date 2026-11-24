# HU-11 · Reserva só de bolsa apta — especificação

> Spec-Driven Development: este arquivo vem **antes** do código. O plano está em [`plan.md`](plan.md) e as tarefas,
> na ordem do TDD, em [`tasks.md`](tasks.md). Contexto: **Estoque** (core). Agregado: **Bolsa**
> ([Seção B](../../arquitetura_SO/B-modelo-tatico.md)).

## Problema

Hoje `BolsaHemocomponente.reservar()` muda o status para `RESERVADA` sem conferir nada, e `alterarStatus(RESERVADA)`
não confere a temperatura. Uma bolsa com a cadeia fria rompida pode ser separada para um paciente.

## História

**Como** responsável técnica do banco de sangue (papel `ADMIN`), **quero** reservar para uma requisição apenas bolsas
aptas, **para que** nenhuma bolsa vencida ou fora da faixa de temperatura seja separada para um paciente.

## Linguagem ubíqua

| Termo | Significado neste contexto |
| :--- | :--- |
| Bolsa apta | `DISPONIVEL`, dentro da validade (vencer hoje ainda vale) e dentro da faixa de temperatura do componente |
| Reservar | Separar uma bolsa apta para uma requisição; passa a `RESERVADA` e gera uma movimentação |
| Movimentação | Registro de uma mudança de status: de, para, quando e a requisição |

## Invariantes (devem valer depois de qualquer operação)

- **I1** O status só muda pelas transições de `StatusBolsa.podePassarPara`.
- **I2** Bolsa vencida só pode ser descartada.
- **I3** Só bolsa apta pode ser reservada.

## Critérios de aceite

| # | Dado | Quando | Então |
| :---: | :--- | :--- | :--- |
| CA1 | Bolsa `DISPONIVEL`, na validade e na faixa | reservo para `R-001` | fica `RESERVADA` e a movimentação aponta `R-001` |
| CA2 | Bolsa que vence hoje | reservo | fica `RESERVADA` |
| CA3 | Bolsa vencida em 30/09/2026 (hoje 01/10/2026) | reservo | recusa: "A bolsa CH-1090 venceu em 30/09/2026 e só pode ser descartada." |
| CA4 | Bolsa de hemácias a 9.5 °C | reservo | recusa: "A bolsa CH-1077 está a 9.5 °C, fora da faixa de 2.0 °C a 6.0 °C do componente HEMACIAS, e não pode ser reservada." |
| CA5 | Bolsa `UTILIZADA` | reservo | recusa: "A bolsa CH-1043 está UTILIZADA e não pode passar para RESERVADA." |
| CA6 | Qualquer recusa pela API | `POST` de reserva | 422 com `ErroDTO`; sem papel `ADMIN`, 403 |

Em toda recusa a bolsa **não muda** (nem status, nem movimentação).

## Contrato da API

```yaml
/api/v1/hemocomponentes/{id}/reserva:
  post:
    summary: Reserva uma bolsa apta para uma requisição (HU-11)
    parameters:
      - { in: header, name: X-Usuario-Id, required: true, schema: { type: string } }
    requestBody:
      content:
        application/json:
          schema: { type: object, required: [requisicaoId], properties: { requisicaoId: { type: string, example: R-001 } } }
    responses:
      '200': { description: Bolsa reservada (BolsaHemocomponenteDTO) }
      '403': { description: Usuário sem papel ADMIN (ErroDTO) }
      '404': { description: Bolsa não encontrada (ErroDTO) }
      '422': { description: Bolsa não está apta (ErroDTO) }
```

## Cenário de aceitação

[`HU-11-reserva-de-bolsa-apta.feature`](../../arquitetura_SO/HU-11-reserva-de-bolsa-apta.feature)

## Fora do escopo

Reservar várias bolsas de uma vez (é a saga da Seção D) e liberar a reserva.
