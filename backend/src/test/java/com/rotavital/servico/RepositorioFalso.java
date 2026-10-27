package com.rotavital.servico;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Function;

/**
 * Repositório falso para testar os serviços sem banco de dados e sem bibliotecas extras.
 * Responde só aos métodos informados; qualquer outro lança erro, o que deixa claro no teste
 * qual consulta o serviço faz.
 */
public final class RepositorioFalso
{
    private RepositorioFalso()
    {
    }

    public static <T> T respondendo(Class<T> tipoDoRepositorio, String metodo, Function<Object[], Object> resposta)
    {
        return respondendo(tipoDoRepositorio, Map.of(metodo, resposta));
    }

    // Para serviços que fazem mais de uma consulta (ex.: o cadastro de bolsa busca ids, confere o lote e grava).
    public static <T> T respondendo(Class<T> tipoDoRepositorio, Map<String, Function<Object[], Object>> respostas)
    {
        Object repositorio = Proxy.newProxyInstance(
                tipoDoRepositorio.getClassLoader(),
                new Class<?>[] { tipoDoRepositorio },
                (proxy, chamado, argumentos) ->
                {
                    Function<Object[], Object> resposta = respostas.get(chamado.getName());

                    if (resposta == null)
                    {
                        throw new UnsupportedOperationException("Consulta inesperada no teste: " + chamado.getName());
                    }

                    return resposta.apply(argumentos);
                });

        return tipoDoRepositorio.cast(repositorio);
    }
}
