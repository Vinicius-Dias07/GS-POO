package br.com.fiap.tpi.domain;

import br.com.fiap.tpi.exception.DadosInvalidosException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ponto vulnerável do território (ex.: trecho baixo de um córrego).
 * Agrupa os sensores instalados naquele local.
 */
public class PontoMonitoramento extends ComponenteTerritorial {

    private final List<Sensor> sensores = new ArrayList<>();

    public PontoMonitoramento(String id, String nome, String regiao) throws DadosInvalidosException {
        super(id, nome, regiao);
    }

    public void adicionarSensor(Sensor sensor) throws DadosInvalidosException {
        if (sensor == null) {
            throw new DadosInvalidosException("Não é possível adicionar um sensor nulo ao ponto " + getNome());
        }
        sensores.add(sensor);
    }

    /** Lista somente leitura: os sensores só entram pelo método adicionarSensor. */
    public List<Sensor> getSensores() {
        return Collections.unmodifiableList(sensores);
    }

    @Override
    public String verificarStatus() {
        long operantes = sensores.stream().filter(Sensor::estaFuncionando).count();
        return "Ponto " + getNome() + " (" + getRegiao() + "): "
                + operantes + " de " + sensores.size() + " sensores operantes";
    }
}
