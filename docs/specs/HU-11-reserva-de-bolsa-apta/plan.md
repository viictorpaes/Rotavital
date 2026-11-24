# HU-11 · Plano

| Peça | Camada (Clean Architecture) | Pacote | Nova? |
| :--- | :--- | :--- | :---: |
| `Bolsa.reservarPara(RequisicaoId, LocalDate)` | Domínio (entidades) | `estoque.negocio.dominio` | método novo |
| `Temperatura.dentroDaFaixaDe(TipoComponente)` | Domínio (objeto de valor) | `estoque.negocio.dominio` | sim |
| `Movimentacao` | Domínio (entidade do agregado) | `estoque.negocio.dominio` | sim |
| `ReservarBolsa` (caso de uso) e `ReservarBolsaComando` | Aplicação | `estoque.negocio.aplicacao` | sim |
| `BolsaRepository` (porta de saída) | Aplicação | `estoque.negocio.aplicacao` | sim |
| `BolsaRepositoryJpa`, `BolsaEntity`, `MovimentacaoEntity`, `BolsaMapper` | Dados (adaptadores) | `estoque.dados` | sim |
| `POST /api/v1/hemocomponentes/{id}/reserva` em `MovimentacaoDeBolsaController` | Apresentação (adaptador de entrada) | `estoque.apresentacao` | endpoint novo |
| Migration `movimentacao_bolsa` | Dados | `supabase/migrations` | sim |

**Regra de dependência:** `ReservarBolsa` só conhece `Bolsa` e `BolsaRepository`; `BolsaRepositoryJpa` implementa a
porta; nenhuma classe de `estoque.negocio.dominio` importa Spring, JPA ou Hibernate (o ArchUnit confere).
