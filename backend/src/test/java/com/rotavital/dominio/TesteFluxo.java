package com.rotavital.dominio;
import java.time.LocalDate;
import java.util.List;

/**
 * Classe de teste (main) que simula manualmente o fluxo básico do domínio:
 * cria um banco de sangue com bolsas em estoque, cria um hospital,
 * simula uma requisição e faz a alocação de uma bolsa compatível.
 *
 * Não é um teste automatizado (JUnit) - é só uma demonstração do fluxo,
 * útil para validar que o modelo de domínio funciona antes de integrar
 * com Spring Boot / persistência nas próximas sprints.
 */
public class TesteFluxo {

    public static void main(String[] args) {

        // 1. Criar o banco de sangue
        Endereco enderecoBanco = new Endereco("Av. Central, 100 - Recife/PE", -8.0578, -34.8829);
        BancoDeSangue banco = new BancoDeSangue("BS-01", "Hemope Central", enderecoBanco);

        // 2. Popular o estoque com algumas bolsas
        BolsaHemocomponente bolsa1 = new BolsaHemocomponente(
                "BOLSA-001",
                TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO,
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(35),
                450.0,
                banco
        );

        BolsaHemocomponente bolsa2 = new BolsaHemocomponente(
                "BOLSA-002",
                TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO,
                LocalDate.now().minusDays(20),
                LocalDate.now().plusDays(5),
                450.0,
                banco
        );

        BolsaHemocomponente bolsa3 = new BolsaHemocomponente(
                "BOLSA-003",
                TipoComponente.PLASMA,
                TipoSanguineo.A_NEGATIVO,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(60),
                250.0,
                banco
        );

        banco.getEstoque().adicionarBolsa(bolsa1);
        banco.getEstoque().adicionarBolsa(bolsa2);
        banco.getEstoque().adicionarBolsa(bolsa3);

        System.out.println("=== Estoque inicial do " + banco.getNome() + " ===");
        banco.getEstoque().getBolsas().forEach(System.out::println);

        // 3. Criar o hospital
        Endereco enderecoHospital = new Endereco("Rua das Flores, 500 - Recife/PE", -8.0476, -34.8770);
        Hospital hospital = new Hospital("HOSP-01", "Hospital das Clinicas", enderecoHospital);

        // 4. Hospital faz uma requisição
        RequisicaoHospitalar requisicao = hospital.solicitar(
                TipoComponente.HEMACIAS,
                TipoSanguineo.O_POSITIVO,
                1
        );

        System.out.println("\n=== Nova requisicao ===");
        System.out.println(requisicao);

        // 5. Buscar bolsas compatíveis no estoque (priorizando a que vence primeiro - FEFO)
        List<BolsaHemocomponente> compativeis = banco.getEstoque().buscarDisponiveis(
                requisicao.getTipoComponente(),
                requisicao.getTipoSanguineo()
        );

        if (compativeis.isEmpty()) {
            System.out.println("Nenhuma bolsa compativel disponivel. Requisicao permanece PENDENTE.");
        } else {
            BolsaHemocomponente escolhida = compativeis.stream()
                    .min((a, b) -> a.getDataValidade().compareTo(b.getDataValidade()))
                    .get();

            escolhida.reservar();
            requisicao.marcarComoAlocada();

            System.out.println("\n=== Alocacao realizada (FEFO) ===");
            System.out.println("Bolsa escolhida: " + escolhida);
            System.out.println("Status da requisicao: " + requisicao.getStatus());
        }

        // 6. Demonstrar o uso da interface PontoDeRede (polimorfismo)
        System.out.println("\n=== Pontos da rede (via interface PontoDeRede) ===");
        List<PontoDeRede> pontos = List.of(banco, hospital);
        for (PontoDeRede ponto : pontos) {
            System.out.println(ponto.getNome() + " -> lat=" + ponto.getLatitude() + ", lon=" + ponto.getLongitude());
        }

        // 7. Verificar bolsas vencidas no estoque
        System.out.println("\n=== Bolsas vencidas hoje ===");
        List<BolsaHemocomponente> vencidas = banco.getEstoque().listarVencidas(LocalDate.now());
        if (vencidas.isEmpty()) {
            System.out.println("Nenhuma bolsa vencida.");
        } else {
            vencidas.forEach(System.out::println);
        }
    }
}