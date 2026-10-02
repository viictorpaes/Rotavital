package com.rotavital.servico;

import java.lang.reflect.Proxy;
import java.util.function.Function;

/**
 * Repositório falso para testar os serviços sem banco de dados e sem bibliotecas extras.
 * Responde só ao método informado; qualquer outro lança erro, o que deixa claro no teste
 * qual consulta o serviço faz.
 */
final class RepositorioFalso
{
    private RepositorioFalso()
    {
    }

    static <T> T respondendo(Class<T> tipoDoRepositorio, String metodo, Function<Object[], Object> resposta)
    {
        Object repositorio = Proxy.newProxyInstance(
                tipoDoRepositorio.getClassLoader(),
                new Class<?>[] { tipoDoRepositorio },
                (proxy, chamado, argumentos) ->
                {
                    if (!chamado.getName().equals(metodo))
                    {
                        throw new UnsupportedOperationException("Consulta inesperada no teste: " + chamado.getName());
                    }

                    return resposta.apply(argumentos);
                });

        return tipoDoRepositorio.cast(repositorio);
    }
}
