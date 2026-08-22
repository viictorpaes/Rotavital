package com.rotavital.dominio;

/**
 * Contrato para qualquer entidade que possa ser um nó no grafo de
 * distribuição (usado pelos algoritmos de rota da disciplina de AED).
 * Hospital e BancoDeSangue implementam essa interface, mas não têm
 * nenhuma relação de herança entre si — só o contrato em comum.
 */
public interface PontoDeRede {
    String getId();
    String getNome();
    double getLatitude();
    double getLongitude();
}
