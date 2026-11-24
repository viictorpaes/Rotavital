# ADR 0001: Decomposição do Rota Vital

Status: aceito · 24/11/2026

## Contexto

O backend é um monolito Spring Boot organizado por camada técnica (`api`, `servico`, `dominio`, `repositorio`), com 42
classes de assuntos diferentes no mesmo pacote `dominio`. A Seção A separou seis bounded contexts (Estoque, Requisição,
Distribuição, Captação, Atendimento, Identidade e Acesso). Pelas premissas da Seção E, mesmo o cenário moderado (40
req/s no pico) cabe em três instâncias de 1 CPU, e o cenário alto em treze. O squad tem oito pessoas, um único pipeline
de CI/CD e uma entrega de código em 01/12/2026. A devolutiva de 17/11 pediu camadas, Clean Architecture e domínio rico,
não distribuição.

## Decisão

Manter **um único deploy (monolito modular)**, com o código separado **por bounded context** e, dentro de cada um, pelas
três camadas da Seção C. Os contextos conversam só pelo serviço de aplicação do outro ou por eventos internos do Spring
(`ApplicationEventPublisher`), nunca mexendo no domínio alheio. A Parte 3 começa pelo Estoque.

## Consequências

- **Melhora:** a fronteira entre contextos fica explícita e testável (ArchUnit), sem pagar rede, broker, saga distribuída
  e vários bancos. Continua uma transação ACID por caso de uso e um só lugar para logs e deploy.
- **Piora:** tudo escala junto. Uma rota pesada (Dijkstra + Google Routes) disputa CPU com o painel do estoque, e uma
  falha que derruba o processo derruba todos os contextos.
- **Saída prevista:** se o cenário alto chegar, a primeira extração é a **Distribuição** (perfil de CPU diferente e
  dependência externa). Como ela só fala com o Estoque pelo serviço aberto e por eventos, a troca de chamada local por
  REST + fila não muda o domínio.
- **Alternativa descartada:** microsserviços já na Parte 3 (Seção D). Custariam várias vezes mais por mês (Seção D.5),
  exigiriam broker, outbox e saga antes de existir carga que justifique, e não cabem em uma semana.
