package br.com.fiap.tpi.service;

import br.com.fiap.tpi.domain.Alerta;
import br.com.fiap.tpi.domain.DadosTerritorio;
import br.com.fiap.tpi.domain.Relatorio;
import br.com.fiap.tpi.domain.ResultadoPrevisao;
import br.com.fiap.tpi.domain.Sensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Padrão criacional BUILDER.
 * O relatório tem várias partes, algumas opcionais (previsões, alertas).
 * Em vez de um construtor com muitos parâmetros, o relatório é montado
 * passo a passo, com nomes que dizem o que cada parte é:
 *
 * <pre>
 * Relatorio r = new RelatorioBuilder()
 *     .comTerritorio(dados)
 *     .comStatusSensores(sensores)
 *     .comUltimasPrevisoes(previsoes)
 *     .comAlertasRecentes(alertas)
 *     .build();
 * </pre>
 */
public class RelatorioBuilder {

    private DadosTerritorio territorio;
    private final List<String> statusSensores = new ArrayList<>();
    private final Map<String, ResultadoPrevisao> ultimasPrevisoes = new LinkedHashMap<>();
    private final List<Alerta> alertasRecentes = new ArrayList<>();

    public RelatorioBuilder comTerritorio(DadosTerritorio territorio) {
        this.territorio = territorio;
        return this;
    }

    public RelatorioBuilder comStatusSensores(List<? extends Sensor> sensores) {
        sensores.forEach(sensor -> statusSensores.add(sensor.verificarStatus()));
        return this;
    }

    /** Chave: nome do sensor; valor: resultado da previsão. */
    public RelatorioBuilder comUltimasPrevisoes(Map<String, ResultadoPrevisao> previsoes) {
        ultimasPrevisoes.putAll(previsoes);
        return this;
    }

    public RelatorioBuilder comAlertasRecentes(List<Alerta> alertas) {
        alertasRecentes.addAll(alertas);
        return this;
    }

    /** Valida as partes obrigatórias e entrega o relatório pronto. */
    public Relatorio build() throws DadosInvalidosException {
        if (territorio == null) {
            throw new DadosInvalidosException("O relatório precisa dos dados do território");
        }
        if (statusSensores.isEmpty()) {
            throw new DadosInvalidosException("O relatório precisa do status de pelo menos um sensor");
        }
        return new Relatorio(territorio, statusSensores, ultimasPrevisoes, alertasRecentes, LocalDateTime.now());
    }
}
