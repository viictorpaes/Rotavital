# language: pt
@HU-04
Funcionalidade: HU-04 — Sugestão de lote por FEFO e compatibilidade ABO/Rh
  Como médico,
  quero que o sistema sugira o lote certo ao preencher a requisição,
  para atender o paciente com uma bolsa compatível, a que vence primeiro, sem escolher na mão.

  Contexto:
    Dado que hoje é 01/10/2026
    E o banco de sangue "BS-01" com as bolsas:
      | id      | componente | tipo | validade   | temperatura | localizacao  |
      | CH-1042 | hemácias   | O-   | 04/10/2026 | 4.0         | R1 · P3 · N2 |
      | CH-1043 | hemácias   | O-   | 21/10/2026 | 3.5         | R1 · P3 · N3 |
      | CH-1061 | hemácias   | A-   | 10/10/2026 | 4.5         | R2 · P2 · N1 |
      | CH-1070 | hemácias   | O+   | 02/10/2026 | 4.0         | R2 · P1 · N2 |
      | CH-1090 | hemácias   | B-   | 30/09/2026 | 4.0         | R2 · P3 · N1 |
      | PQ-3014 | plaquetas  | A+   | 04/10/2026 | 25.6        | R3 · P2 · N1 |

  Cenário: Sugere o lote O- que vence primeiro (exemplo do protótipo)
    Quando peço 1 unidade de hemácias para um paciente "O-" no banco "BS-01"
    Então o sistema sugere os lotes "CH-1042"

  Cenário: Prefere o mesmo tipo do paciente e completa com um compatível
    Quando peço 2 unidades de hemácias para um paciente "A-" no banco "BS-01"
    Então o sistema sugere os lotes "CH-1061, CH-1042"

  Cenário: Nunca sugere hemácias Rh+ para paciente Rh-
    Quando peço 3 unidades de hemácias para um paciente "O-" no banco "BS-01"
    Então o sistema sugere os lotes "CH-1042, CH-1043"
    E a sugestão avisa que faltam 1 unidade

  Cenário: Bolsa vencida não é sugerida
    Quando peço 1 unidade de hemácias para um paciente "B-" no banco "BS-01"
    Então o sistema sugere os lotes "CH-1042"

  Cenário: Bolsa fora da faixa de temperatura não é sugerida
    Quando peço 1 unidade de plaquetas para um paciente "A+" no banco "BS-01"
    Então o sistema não sugere nenhum lote
    E a sugestão avisa que faltam 1 unidade

  Cenário: Quantidade menor que 1
    Quando peço 0 unidades de hemácias para um paciente "O-" no banco "BS-01"
    Então o pedido de sugestão é recusado com status 400
