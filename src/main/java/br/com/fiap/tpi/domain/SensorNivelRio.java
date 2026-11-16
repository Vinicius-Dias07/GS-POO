package br.com.fiap.tpi.domain;

import br.com.fiap.tpi.exception.DadosInvalidosException;

import java.util.Random;

/**
 * Régua/sensor de nível do rio ou córrego (0,5 a 6 m na simulação).
 */
public class SensorNivelRio extends SensorSimulado {

    public SensorNivelRio(String id, String nome, String regiao) throws DadosInvalidosException {
        this(id, nome, regiao, new Random());
    }

    public SensorNivelRio(String id, String nome, String regiao, Random gerador) throws DadosInvalidosException {
        super(id, nome, regiao, 0.5, 6.0, gerador);
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.NIVEL_RIO;
    }

    @Override
    public String verificarStatus() {
        return estaFuncionando()
                ? getNome() + " (" + getRegiao() + "): acompanhando o nível da água"
                : getNome() + " (" + getRegiao() + "): sensor de nível sem resposta — pode estar submerso ou danificado";
    }
}
