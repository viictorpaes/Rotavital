# language: pt
@HU-02
Funcionalidade: HU-02 — Caixa de alertas do painel (validade e temperatura)
  Como médico,
  quero ver no painel os alertas críticos do estoque,
  para tirar de uso a bolsa vencida ou fora da faixa e usar primeiro a que está para vencer.

  Contexto:
    Dado que hoje é 01/10/2026
    E o banco de sangue "BS-01" com as bolsas:
      | id      | componente | tipo | validade   | temperatura | status     |
      | CH-1042 | hemácias   | O-   | 04/10/2026 | 4.0         | disponível |
      | CH-1043 | hemácias   | O-   | 21/10/2026 | 3.5         | disponível |
      | CH-1090 | hemácias   | B-   | 30/09/2026 | 4.0         | disponível |
      | PQ-3014 | plaquetas  | A+   | 05/10/2026 | 25.6        | disponível |
      | CH-1099 | hemácias   | A+   | 29/09/2026 | 4.0         | descartada |

  Cenário: Painel mostra primeiro os alertas críticos
    Quando abro os alertas do banco "BS-01"
    Então vejo os alertas, nesta ordem:
      | lote    | alerta         | nível   |
      | CH-1090 | VENCIDA        | CRITICO |
      | PQ-3014 | FORA_DA_FAIXA  | CRITICO |
      | CH-1042 | VENCE_EM_BREVE | ATENCAO |
      | PQ-3014 | VENCE_EM_BREVE | ATENCAO |
    E o painel conta 2 alertas críticos

  Cenário: Bolsa longe do vencimento ou já descartada não gera alerta
    Quando abro os alertas do banco "BS-01"
    Então não há alerta para os lotes "CH-1043, CH-1099"
