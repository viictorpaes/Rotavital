# ADR 0002: Domínio puro, com portas e adaptadores (Clean Architecture)

Status: aceito · 24/11/2026 · substitui a nota "Por que não Clean Architecture pura" de `docs/ARQUITETURA.md` §3

## Contexto

Onze das 42 classes de `dominio` usam anotações JPA (10 `@Entity` e o `@Embeddable` `Endereco`), quatro usam anotações
do Hibernate e `BolsaHemocomponente` implementa `Persistable`, do Spring Data. A decisão anterior aceitava isso para não
escrever mapeadores. Ao mesmo tempo, regras da bolsa (prazo de validade, volume, faixa do sensor) estão no
`EstoqueService`, e a devolutiva de 17/11 pediu Clean Architecture e um domínio rico, sem entidades anêmicas.

## Decisão

O domínio passa a ser **Java puro**: nenhuma classe de `..negocio.dominio..` importa `jakarta.persistence`,
`org.hibernate` ou `org.springframework`. A persistência fica em `dados`, com classes `*Entity` e um mapeador por
agregado. A aplicação declara as **portas** (`BolsaRepository`, `PublicadorDeEventos`) e os adaptadores as implementam.
Começa pelo agregado `Bolsa`, na Parte 3.

## Consequências

- **Melhora:** a regra de negócio é testada sem Spring nem banco. Os objetos de valor (`Validade`, `Volume`,
  `Temperatura`, `CodigoDaBolsa`) passam a impedir estados inválidos, e trocar o banco ou o framework não toca o domínio.
- **Piora:** mais classes (`BolsaEntity`, `BolsaMapper`) e um mapeamento a manter. Um campo novo passa a exigir mudança
  em dois lugares.
- **Como garantimos:** teste ArchUnit no `mvn verify`, que já roda na esteira: quem quebrar a regra não faz merge.
- **Alternativa descartada:** manter as anotações JPA no domínio e só mover as regras do serviço. É mais barato, mas
  deixa o domínio preso ao Hibernate (construtor vazio obrigatório, `Persistable`) e não responde ao ponto 2 da devolutiva.
