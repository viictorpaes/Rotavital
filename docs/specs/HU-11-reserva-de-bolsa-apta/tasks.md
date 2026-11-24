# HU-11 · Tarefas (ordem do TDD: teste vermelho → código mínimo → refatoração)

| # | Tarefa | Teste que fecha a tarefa |
| :---: | :--- | :--- |
| 1 | `Temperatura` com faixa do sensor e `dentroDaFaixaDe` | `TemperaturaTest`: 9.5 °C está fora da faixa de hemácias; 4.0 °C está dentro |
| 2 | `Validade.venceuEm(hoje)` | `ValidadeTest`: vence hoje não venceu; vencida ontem venceu |
| 3 | `Bolsa.reservarPara` conferindo I1, I2 e I3 | `ReservaDaBolsaTest`: CA1 a CA5 no domínio, sem Spring |
| 4 | `Movimentacao` registrada na reserva | `ReservaDaBolsaTest`: a última movimentação aponta `R-001` |
| 5 | Remover `reservar()` e `descartar()` sem parâmetro | Os testes que usavam passam a montar a bolsa pelo caminho permitido |
| 6 | Caso de uso `ReservarBolsa` com repositório em memória | `ReservarBolsaTest`: salva só quando a bolsa aceita; papel `ADMIN` |
| 7 | Adaptador JPA + migration `movimentacao_bolsa` | `EstoqueIT`: reserva grava status e movimentação no Postgres |
| 8 | Endpoint `POST /api/v1/hemocomponentes/{id}/reserva` | `ReservaDeBolsaApiTest` (MockMvc): 200, 403, 404, 422 |
| 9 | Feature da HU-11 no `AceitacaoIT` | Cucumber verde |
| 10 | ArchUnit e JaCoCo | `mvn verify` falha se o domínio importar JPA ou se a cobertura do domínio cair abaixo de 95 % |
