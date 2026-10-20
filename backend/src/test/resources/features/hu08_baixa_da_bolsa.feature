# language: pt
@HU-08
Funcionalidade: HU-08 — Baixa da bolsa usada no procedimento
  Como médico,
  quero dar baixa na bolsa usada ao concluir o atendimento do paciente,
  para que o estoque reflita com exatidão o que foi realmente consumido.

  Contexto:
    Dado que hoje é 01/10/2026
    E o banco de sangue "BS-01" com as bolsas:
      | id      | componente | tipo | validade   | status     |
      | CH-1042 | hemácias   | O-   | 04/10/2026 | reservada  |
      | CH-1043 | hemácias   | O-   | 21/10/2026 | utilizada  |
      | CH-1090 | hemácias   | B-   | 30/09/2026 | disponível |

  Cenário: Dar baixa da bolsa reservada para o paciente
    Quando registro a bolsa "CH-1042" como "utilizada"
    Então a bolsa "CH-1042" fica "utilizada" no estoque

  Cenário: Bolsa já utilizada não pode ser usada de novo
    Quando registro a bolsa "CH-1043" como "reservada"
    Então a movimentação é recusada com a mensagem "A bolsa CH-1043 está UTILIZADA e não pode passar para RESERVADA."
    E a bolsa "CH-1043" continua "utilizada" no estoque

  Cenário: Bolsa vencida só pode ser descartada
    Quando registro a bolsa "CH-1090" como "reservada"
    Então a movimentação é recusada com a mensagem "A bolsa CH-1090 venceu em 30/09/2026 e só pode ser descartada."
    Quando registro a bolsa "CH-1090" como "descartada"
    Então a bolsa "CH-1090" fica "descartada" no estoque
