package br.com.fiap.tpi.domain;

import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.exception.FonteIndisponivelException;

import java.util.Random;

/**
 * Comportamento comum aos sensores simulados: sorteia um valor dentro de uma
 * faixa realista e, com ~5% de chance, "falha" (não responde).
 * Cada filha informa apenas o seu tipo, a sua faixa e como descreve o status —
 * assim a lógica de simulação não fica repetida em três classes.
 */
public abstract class SensorSimulado extends ComponenteTerritorial implements Sensor {

    private static final double PROBABILIDADE_FALHA = 0.05;

    private final Random gerador;
    private final double minimoSimulado;
    private final double maximoSimulado;

    protected SensorSimulado(String id, String nome, String regiao,
                             double minimoSimulado, double maximoSimulado, Random gerador)
            throws DadosInvalidosException {
        super(id, nome, regiao);
        this.minimoSimulado = minimoSimulado;
        this.maximoSimulado = maximoSimulado;
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
        double valor = minimoSimulado + gerador.nextDouble() * (maximoSimulado - minimoSimulado);
        return Math.round(valor * 10.0) / 10.0;
    }

    @Override
    public boolean estaFuncionando() {
        return getStatus() == StatusComponente.OPERANTE;
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
