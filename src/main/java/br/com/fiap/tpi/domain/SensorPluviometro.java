package br.com.fiap.tpi.domain;

import br.com.fiap.tpi.exception.DadosInvalidosException;

import java.util.Random;

/**
 * Pluviômetro simulado: mede a intensidade da chuva (0 a 80 mm/h na simulação).
 */
public class SensorPluviometro extends SensorSimulado {

    public SensorPluviometro(String id, String nome, String regiao) throws DadosInvalidosException {
        this(id, nome, regiao, new Random());
    }

    /** Permite informar o gerador aleatório (útil para testes previsíveis). */
    public SensorPluviometro(String id, String nome, String regiao, Random gerador) throws DadosInvalidosException {
        super(id, nome, regiao, 0.0, 80.0, gerador);
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.CHUVA;
    }

    @Override
    public String verificarStatus() {
        return estaFuncionando()
                ? getNome() + " (" + getRegiao() + "): medindo chuva normalmente"
                : getNome() + " (" + getRegiao() + "): pluviômetro sem resposta — verificar equipamento";
    }
}
