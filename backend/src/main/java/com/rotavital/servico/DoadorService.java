package com.rotavital.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rotavital.api.dto.doador.AtualizarPerfilDoadorRequest;
import com.rotavital.api.dto.doador.NovoDoadorRequest;
import com.rotavital.dominio.AgendamentoDoacao;
import com.rotavital.dominio.PapelUsuario;
import com.rotavital.dominio.StatusAgendamento;
import com.rotavital.dominio.Usuario;
import com.rotavital.repositorio.AgendamentoDoacaoRepository;
import com.rotavital.repositorio.UsuarioRepository;

/**
 * Cadastro e perfil do doador (PI3-203). O doador cria a conta pelo portal: a senha vai para o
 * Supabase Auth e os dados ficam na tabela usuario, com papel DOADOR. Depois ele consulta e corrige
 * os próprios dados. Médico e admin continuam sendo cadastrados pela equipe (supabase/usuarios_de_teste.sql).
 */
@Service
public class DoadorService
{
    // Faixa de idade para doar sangue no Brasil (Ministério da Saúde); menores de 18 com autorização dos pais.
    public static final int IDADE_MINIMA = 16;
    public static final int IDADE_MAXIMA = 69;
    // O mínimo que o Supabase Auth aceita por padrão.
    public static final int TAMANHO_MINIMO_SENHA = 6;
    private static final int TAMANHO_MAXIMO_NOME = 120;
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String JA_CADASTRADO = "Já existe uma conta com este e-mail. Entre com a sua senha.";

    private final CriadorDeContas contas;
    private final VerificadorDeCredenciais verificador;
    private final UsuarioRepository usuarios;
    private final AgendamentoDoacaoRepository agendamentos;
    private final IdentificacaoService identificacao;
    private final Clock relogio;

    public DoadorService(CriadorDeContas contas, VerificadorDeCredenciais verificador, UsuarioRepository usuarios,
            AgendamentoDoacaoRepository agendamentos, IdentificacaoService identificacao, Clock relogio)
    {
        this.contas = contas;
        this.verificador = verificador;
        this.usuarios = usuarios;
        this.agendamentos = agendamentos;
        this.identificacao = identificacao;
        this.relogio = relogio;
    }

    /**
     * Cria a conta no Supabase Auth e a linha de usuario. Dado faltando ou fora da regra lança
     * DadosInvalidosException (400); e-mail que já tem conta lança RegraDeNegocioException (422).
     * Tudo é conferido antes de chamar o Supabase, para não deixar conta criada sem perfil.
     */
    @Transactional
    public DoadorCadastrado cadastrar(NovoDoadorRequest dados)
    {
        if (dados == null)
        {
            throw new DadosInvalidosException("O corpo da requisição é obrigatório.");
        }

        List<String> problemas = new ArrayList<>();
        String nome = validarNome(dados.nome(), problemas);
        String email = validarEmail(dados.email(), problemas);
        validarSenha(dados.senha(), problemas);
        validarDataNascimento(dados.dataNascimento(), problemas);
        String telefone = validarTelefone(dados.telefone(), problemas);
        lancarSeHouver(problemas);

        if (usuarios.existsByEmailIgnoreCase(email))
        {
            throw new RegraDeNegocioException(JA_CADASTRADO);
        }

        ContaCriada conta = contas.criar(email, dados.senha()).orElseGet(() -> contaSemPerfil(email, dados.senha()));

        Usuario doador = usuarios.save(Usuario.novoDoador(conta.authUserId(), nome, email, dados.tipoSanguineo(),
                dados.dataNascimento(), telefone));
        return new DoadorCadastrado(doador, conta.sessao());
    }

    @Transactional(readOnly = true)
    public PerfilDoador perfil(UUID usuarioId)
    {
        Usuario doador = identificacao.exigir(usuarioId, "ver o seu perfil", PapelUsuario.DOADOR);
        return montarPerfil(doador);
    }

    // O doador corrige nome, data de nascimento, tipo sanguíneo e telefone. Mesmas regras do cadastro.
    @Transactional
    public PerfilDoador atualizar(UUID usuarioId, AtualizarPerfilDoadorRequest dados)
    {
        Usuario doador = identificacao.exigir(usuarioId, "alterar o seu perfil", PapelUsuario.DOADOR);
        if (dados == null)
        {
            throw new DadosInvalidosException("O corpo da requisição é obrigatório.");
        }

        List<String> problemas = new ArrayList<>();
        String nome = validarNome(dados.nome(), problemas);
        validarDataNascimento(dados.dataNascimento(), problemas);
        String telefone = validarTelefone(dados.telefone(), problemas);
        lancarSeHouver(problemas);

        doador.atualizarPerfil(nome, dados.tipoSanguineo(), dados.dataNascimento(), telefone);
        return montarPerfil(usuarios.save(doador));
    }

    /**
     * O e-mail já tem conta no Supabase Auth, mas nenhuma linha em usuario: um cadastro que parou no
     * meio, ou feito com o Postgres local do Compose. Com a senha certa, o cadastro termina agora;
     * senão, é só um e-mail que já tem dono.
     */
    private ContaCriada contaSemPerfil(String email, String senha)
    {
        SessaoAutenticada sessao = verificador.verificar(email, senha)
                .orElseThrow(() -> new RegraDeNegocioException(JA_CADASTRADO));

        if (usuarios.findByAuthUserId(sessao.authUserId()).isPresent())
        {
            throw new RegraDeNegocioException(JA_CADASTRADO);
        }
        return new ContaCriada(sessao.authUserId(), sessao);
    }

    private PerfilDoador montarPerfil(Usuario doador)
    {
        List<AgendamentoDoacao> doDoador = agendamentos.findByDoadorIdOrderByAgendadoParaDesc(doador.getId());

        List<OffsetDateTime> realizadas = doDoador.stream()
                .filter(agendamento -> agendamento.getStatus() == StatusAgendamento.REALIZADO)
                .map(AgendamentoDoacao::getAgendadoPara)
                .toList();
        OffsetDateTime proxima = doDoador.stream()
                .filter(AgendamentoDoacao::estaEmAberto)
                .map(AgendamentoDoacao::getAgendadoPara)
                .findFirst()
                .orElse(null);

        return new PerfilDoador(doador, doador.idadeEm(LocalDate.now(relogio)), realizadas.size(),
                realizadas.isEmpty() ? null : realizadas.get(0), proxima);
    }

    private static String validarNome(String nome, List<String> problemas)
    {
        String limpo = nome == null ? "" : nome.trim().replaceAll("\\s+", " ");
        if (limpo.isEmpty())
        {
            problemas.add("O campo 'nome' é obrigatório.");
        }
        else if (limpo.length() > TAMANHO_MAXIMO_NOME)
        {
            problemas.add("O nome pode ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        return limpo;
    }

    private static String validarEmail(String email, List<String> problemas)
    {
        String limpo = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (limpo.isEmpty())
        {
            problemas.add("O campo 'email' é obrigatório.");
        }
        else if (!FORMATO_EMAIL.matcher(limpo).matches())
        {
            problemas.add("O e-mail " + limpo + " não é válido.");
        }
        return limpo;
    }

    private static void validarSenha(String senha, List<String> problemas)
    {
        if (senha == null || senha.isEmpty())
        {
            problemas.add("O campo 'senha' é obrigatório.");
        }
        else if (senha.length() < TAMANHO_MINIMO_SENHA)
        {
            problemas.add("A senha precisa ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
    }

    private void validarDataNascimento(LocalDate dataNascimento, List<String> problemas)
    {
        if (dataNascimento == null)
        {
            problemas.add("O campo 'dataNascimento' é obrigatório.");
            return;
        }

        LocalDate hoje = LocalDate.now(relogio);
        if (!dataNascimento.isBefore(hoje))
        {
            problemas.add("A data de nascimento precisa ser no passado.");
            return;
        }

        int idade = Period.between(dataNascimento, hoje).getYears();
        if (idade < IDADE_MINIMA || idade > IDADE_MAXIMA)
        {
            problemas.add("Para doar sangue é preciso ter entre " + IDADE_MINIMA + " e " + IDADE_MAXIMA
                    + " anos (pela data informada, você tem " + idade + ").");
        }
    }

    // Aceita "(81) 99876-5432" e grava só os dígitos, com DDD: 10 (fixo) ou 11 (celular). Vazio vira nulo.
    private static String validarTelefone(String telefone, List<String> problemas)
    {
        if (telefone == null || telefone.isBlank())
        {
            return null;
        }

        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() < 10 || digitos.length() > 11)
        {
            problemas.add("O telefone precisa ter DDD e 8 ou 9 dígitos, como (81) 99876-5432.");
        }
        return digitos;
    }

    private static void lancarSeHouver(List<String> problemas)
    {
        if (!problemas.isEmpty())
        {
            throw new DadosInvalidosException(String.join(" ", problemas));
        }
    }
}
