# language: pt
# Trabalho U2 · Parte 2 · Seção B. Cenário da história âncora (invariante I3 do agregado Bolsa).
# Na Parte 3 este arquivo vai para backend/src/test/resources/features/ e roda no AceitacaoIT.
@HU-11
Funcionalidade: HU-11 — Reserva só de bolsa apta
  Como responsável técnica do banco de sangue,
  quero reservar para uma requisição apenas bolsas aptas,
  para que nenhuma bolsa vencida ou com a cadeia fria rompida seja separada para um paciente.

  Contexto:
    Dado que hoje é 01/10/2026
    E o banco de sangue "BS-01" com as bolsas:
      | id      | componente | tipo | validade   | temperatura | status     |
      | CH-1042 | hemácias   | O-   | 20/10/2026 | 4.0         | disponível |
      | CH-1050 | hemácias   | O-   | 01/10/2026 | 4.0         | disponível |
      | CH-1090 | hemácias   | B-   | 30/09/2026 | 4.0         | disponível |
      | CH-1077 | hemácias   | A+   | 25/10/2026 | 9.5         | disponível |
      | CH-1043 | hemácias   | O-   | 21/10/2026 | 4.0         | utilizada  |

  # CA1
  Cenário: Reservar bolsa apta para a requisição
    Quando reservo a bolsa "CH-1042" para a requisição "R-001"
    Então a bolsa "CH-1042" fica "reservada" no estoque
    E a última movimentação da bolsa "CH-1042" aponta a requisição "R-001"

  # CA2
  Cenário: Bolsa que vence hoje ainda pode ser reservada
    Quando reservo a bolsa "CH-1050" para a requisição "R-001"
    Então a bolsa "CH-1050" fica "reservada" no estoque

  # CA3 e CA4
  Esquema do Cenário: Bolsa que não está apta não é reservada
    Quando reservo a bolsa "<bolsa>" para a requisição "R-001"
    Então a movimentação é recusada com a mensagem "<mensagem>"
    E a bolsa "<bolsa>" continua "disponível" no estoque

    Exemplos:
      | bolsa   | mensagem                                                                                                           |
      | CH-1090 | A bolsa CH-1090 venceu em 30/09/2026 e só pode ser descartada.                                                     |
      | CH-1077 | A bolsa CH-1077 está a 9.5 °C, fora da faixa de 2.0 °C a 6.0 °C do componente HEMACIAS, e não pode ser reservada. |

  # CA5
  Cenário: Bolsa que não está disponível não é reservada
    Quando reservo a bolsa "CH-1043" para a requisição "R-001"
    Então a movimentação é recusada com a mensagem "A bolsa CH-1043 está UTILIZADA e não pode passar para RESERVADA."
    E a bolsa "CH-1043" continua "utilizada" no estoque
