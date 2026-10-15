package com.rotavital.api;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.rotavital.api.dto.comum.ErroDTO;
import com.rotavital.servico.DadosInvalidosException;
import com.rotavital.servico.RecursoNaoEncontradoException;
import com.rotavital.servico.RegraDeNegocioException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Converte os erros dos controllers em ErroDTO (RFC 7807), com uma mensagem em detail que a tela
 * pode mostrar como está. Sem isto, valor fora do enum, data em outro formato ou JSON quebrado
 * caem na resposta padrão do Spring, que não diz qual campo está errado.
 */
@RestControllerAdvice
public class TratadorDeErros
{
    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroDTO> dadosInvalidos(DadosInvalidosException e, HttpServletRequest request)
    {
        return erro(400, "Requisição inválida", e.getMessage(), request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroDTO> recursoNaoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest request)
    {
        return erro(404, "Recurso não encontrado", e.getMessage(), request);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroDTO> regraDeNegocio(RegraDeNegocioException e, HttpServletRequest request)
    {
        return erro(422, "Regra de negócio violada", e.getMessage(), request);
    }

    // Valor de query string ou de caminho que não converte para o tipo esperado (ex.: ?tipoSanguineo=XYZ).
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroDTO> parametroInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest request)
    {
        String detalhe = "O parâmetro '" + e.getName() + "' não aceita o valor '" + e.getValue() + "'."
                + valoresAceitos(e.getRequiredType());
        return erro(400, "Requisição inválida", detalhe, request);
    }

    // Corpo que o Jackson não consegue ler: campo com valor do tipo errado, JSON quebrado ou corpo vazio.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> corpoInvalido(HttpMessageNotReadableException e, HttpServletRequest request)
    {
        String detalhe = "O corpo da requisição está vazio ou não é um JSON válido.";

        if (e.getCause() instanceof MismatchedInputException campoErrado && !campoErrado.getPath().isEmpty())
        {
            String campo = campoErrado.getPath().stream()
                    .map(Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));
            String recusado = campoErrado instanceof InvalidFormatException formato
                    ? "o valor '" + formato.getValue() + "'"
                    : "esse tipo de valor";

            detalhe = "O campo '" + campo + "' não aceita " + recusado + "."
                    + valoresAceitos(campoErrado.getTargetType());
        }

        return erro(400, "Requisição inválida", detalhe, request);
    }

    // Diz o que mandar no lugar do valor recusado, para quem lê a mensagem não precisar abrir o contrato.
    private static String valoresAceitos(Class<?> tipo)
    {
        if (tipo == null)
        {
            return "";
        }
        if (tipo.isEnum())
        {
            return " Valores aceitos: " + Arrays.stream(tipo.getEnumConstants())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", ")) + ".";
        }
        if (tipo == LocalDate.class)
        {
            return " Use uma data no formato AAAA-MM-DD.";
        }
        if (Number.class.isAssignableFrom(ClassUtils.resolvePrimitiveIfNecessary(tipo)))
        {
            return " Use um número.";
        }
        return "";
    }

    private static ResponseEntity<ErroDTO> erro(int status, String titulo, String detalhe, HttpServletRequest request)
    {
        ErroDTO erro = new ErroDTO(null, titulo, status, detalhe, request.getRequestURI());
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(erro);
    }
}
