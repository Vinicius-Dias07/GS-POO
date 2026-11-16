package br.com.fiap.tpi.domain;

import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.exception.FonteIndisponivelException;

import java.util.Random;

/**
 * Pluviômetro simulado: mede a intensidade da chuva em mm/h.
 * É um componente do território (herança) e também um sensor (interface).
 */
public class SensorPluviometro extends ComponenteTerritorial implements Sensor {

    private static final double MINIMO_SIMULADO = 0.0;
    private static final double MAXIMO_SIMULADO = 80.0;
    private static final double PROBABILIDADE_FALHA = 0.05;

    private final Random gerador;

    public SensorPluviometro(String id, String nome, String regiao) throws DadosInvalidosException {
        this(id, nome, regiao, new Random());
    }

    /** Permite informar o gerador aleatório (útil para testes previsíveis). */
    public SensorPluviometro(String id, String nome, String regiao, Random gerador) throws DadosInvalidosException {
        super(id, nome, regiao);
        this.gerador = gerador;
    }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (gerador.nextDouble() < PROBABILIDADE_FALHA) {
            marcarStatus(StatusComponente.FALHA);
            throw new FonteIndisponivelException(
                    "Sensor " + getNome() + " da região " + getRegiao() + " não respondeu");
        }
        marcarStatus(StatusComponente.OPERANTE);
        double valor = MINIMO_SIMULADO + gerador.nextDouble() * (MAXIMO_SIMULADO - MINIMO_SIMULADO);
        return Math.round(valor * 10.0) / 10.0;
    }

    @Override
    public boolean estaFuncionando() {
        return getStatus() == StatusComponente.OPERANTE;
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.CHUVA;
    }

    @Override
    public String verificarStatus() {
        return estaFuncionando()
                ? "Pluviômetro " + getNome() + " (" + getRegiao() + "): medindo chuva normalmente"
                : "Pluviômetro " + getNome() + " (" + getRegiao() + "): sem resposta — verificar equipamento";
    }

    private void marcarStatus(StatusComponente novoStatus) {
        try {
            setStatus(novoStatus);
        } catch (DadosInvalidosException e) {
            // Não acontece: o status passado nunca é nulo. Se acontecer, é erro de programação.
            throw new IllegalStateException("Status inválido para o sensor " + getId(), e);
        }
    }
}
