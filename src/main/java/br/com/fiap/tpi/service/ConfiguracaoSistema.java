package br.com.fiap.tpi.service;

import br.com.fiap.tpi.domain.NivelAlerta;
import br.com.fiap.tpi.domain.TipoSensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Guarda os limiares de alerta por tipo de sensor e nível.
 * É um bean comum do Spring (uma instância gerenciada pelo container),
 * não o padrão Singleton — a equipe escolheu Factory e Builder como padrões.
 */
@Component
public class ConfiguracaoSistema {

    private final Map<TipoSensor, Map<NivelAlerta, Double>> limiares = new EnumMap<>(TipoSensor.class);

    public ConfiguracaoSistema() {
        registrarPadrao(TipoSensor.CHUVA, 20.0, 40.0, 60.0);       // mm/h
        registrarPadrao(TipoSensor.NIVEL_RIO, 3.0, 4.0, 5.0);      // m
        registrarPadrao(TipoSensor.UMIDADE_SOLO, 70.0, 85.0, 95.0); // %
    }

    public double getLimiar(TipoSensor tipo, NivelAlerta nivel) {
        return limiares.get(tipo).get(nivel);
    }

    /**
     * Altera um limiar mantendo a coerência: dentro da faixa do sensor e
     * em ordem crescente (ATENÇÃO &lt; ALERTA &lt; CRÍTICO).
     */
    public void definirLimiar(TipoSensor tipo, NivelAlerta nivel, double valor) throws DadosInvalidosException {
        if (tipo == null || nivel == null) {
            throw new DadosInvalidosException("Tipo de sensor e nível de alerta são obrigatórios");
        }
        if (!tipo.estaNaFaixaValida(valor)) {
            throw new DadosInvalidosException("Limiar de " + nivel.getDescricao() + " para " + tipo.getDescricao()
                    + " fora da faixa válida (" + tipo.getMinimoValido() + " a " + tipo.getMaximoValido()
                    + " " + tipo.getUnidade() + "): " + valor);
        }
        Map<NivelAlerta, Double> doTipo = limiares.get(tipo);
        for (NivelAlerta outro : NivelAlerta.values()) {
            boolean abaixoDeveSerMenor = outro.ordinal() < nivel.ordinal() && doTipo.get(outro) >= valor;
            boolean acimaDeveSerMaior = outro.ordinal() > nivel.ordinal() && doTipo.get(outro) <= valor;
            if (abaixoDeveSerMenor || acimaDeveSerMaior) {
                throw new DadosInvalidosException("Limiar incoerente: " + nivel.getDescricao() + " = " + valor
                        + " conflita com " + outro.getDescricao() + " = " + doTipo.get(outro)
                        + " (a ordem deve ser Atenção < Alerta < Crítico)");
            }
        }
        doTipo.put(nivel, valor);
    }

    private void registrarPadrao(TipoSensor tipo, double atencao, double alerta, double critico) {
        Map<NivelAlerta, Double> doTipo = new EnumMap<>(NivelAlerta.class);
        doTipo.put(NivelAlerta.ATENCAO, atencao);
        doTipo.put(NivelAlerta.ALERTA, alerta);
        doTipo.put(NivelAlerta.CRITICO, critico);
        limiares.put(tipo, doTipo);
    }
}
