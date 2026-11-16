package br.com.fiap.tpi.domain;

import br.com.fiap.tpi.exception.DadosInvalidosException;

import java.util.Random;

/**
 * Sensor de umidade do solo (20 a 100% na simulação).
 * Solo saturado não absorve mais água: a chuva vira enxurrada.
 */
public class SensorUmidadeSolo extends SensorSimulado {

    public SensorUmidadeSolo(String id, String nome, String regiao) throws DadosInvalidosException {
        this(id, nome, regiao, new Random());
    }

    public SensorUmidadeSolo(String id, String nome, String regiao, Random gerador) throws DadosInvalidosException {
        super(id, nome, regiao, 20.0, 100.0, gerador);
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.UMIDADE_SOLO;
    }

    @Override
    public String verificarStatus() {
        return estaFuncionando()
                ? getNome() + " (" + getRegiao() + "): medindo a saturação do solo"
                : getNome() + " (" + getRegiao() + "): sensor de umidade sem resposta — verificar equipamento";
    }
}
