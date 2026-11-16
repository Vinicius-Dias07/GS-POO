package br.com.fiap.tpi.dominio;

import br.com.fiap.tpi.excecao.FonteIndisponivelException;

/**
 * Contrato comum a todos os sensores. Quem usa um sensor não precisa saber
 * se ele mede chuva, nível do rio ou umidade do solo.
 */
public interface Sensor {

    double lerValor() throws FonteIndisponivelException;

    boolean estaFuncionando();

    TipoSensor getTipo();

    // Os métodos abaixo já são implementados por ComponenteTerritorial.
    // Declará-los aqui permite usar qualquer Sensor sem precisar de cast.

    String getId();

    String getNome();

    String getRegiao();

    String verificarStatus();
}
