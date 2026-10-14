# language: pt
@HU-03
Funcionalidade: HU-03 — Estoque por tipo sanguíneo com detalhe de lote
  Como médico,
  quero consultar o estoque agrupado por tipo sanguíneo, com o detalhe de cada lote,
  para saber quais lotes estão em risco de vencer ou fora da faixa de temperatura ideal.

  # Estes cenários são de caracterização: a HU-03 já existia antes do TDD.
  # Eles foram escritos depois do código, a partir dos cenários BDD do README (Entrega 01).

  Contexto:
    Dado o banco de sangue "BS-01" com as bolsas:
      | id      | componente      | tipo | validade   | temperatura | localizacao  |
      | CH-1043 | hemácias        | O-   | 21/10/2027 | 3.5         | R1 · P3 · N3 |
      | CH-1042 | hemácias        | O-   | 04/10/2027 | 4.0         | R1 · P3 · N2 |
      | CR-4002 | crioprecipitado | O-   | 21/09/2028 | -25.0       | F2 · P1 · N1 |
      | PQ-3014 | plaquetas       | A+   | 04/10/2027 | 25.6        | R3 · P2 · N1 |

  Cenário: Filtrar o estoque por um tipo sanguíneo
    Quando consulto o estoque do banco "BS-01" filtrando pelo tipo "O-"
    Então vejo só os lotes "CH-1042, CH-1043, CR-4002", da validade mais próxima para a mais distante

  Cenário: Lote fora da faixa de temperatura
    Quando consulto o estoque do banco "BS-01" filtrando pelo tipo "A+"
    Então o lote "PQ-3014" aparece marcado como fora da faixa de temperatura
