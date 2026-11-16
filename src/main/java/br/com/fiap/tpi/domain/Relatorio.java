package br.com.fiap.tpi.domain;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Relatório de status completo do território. Imutável: só tem getters e
 * formatar(). É montado passo a passo pelo RelatorioBuilder.
 */
public class Relatorio {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale PORTUGUES_BRASIL = Locale.of("pt", "BR");

    private final DadosTerritorio territorio;
    private final List<String> statusSensores;
    private final Map<String, ResultadoPrevisao> ultimasPrevisoes;
    private final List<Alerta> alertasRecentes;
    private final LocalDateTime geradoEm;

    public Relatorio(DadosTerritorio territorio, List<String> statusSensores,
                     Map<String, ResultadoPrevisao> ultimasPrevisoes, List<Alerta> alertasRecentes,
                     LocalDateTime geradoEm) {
        this.territorio = territorio;
        this.statusSensores = List.copyOf(statusSensores);
        this.ultimasPrevisoes = Collections.unmodifiableMap(new LinkedHashMap<>(ultimasPrevisoes));
        this.alertasRecentes = List.copyOf(alertasRecentes);
        this.geradoEm = geradoEm;
    }

    public DadosTerritorio getTerritorio() {
        return territorio;
    }

    public List<String> getStatusSensores() {
        return statusSensores;
    }

    public Map<String, ResultadoPrevisao> getUltimasPrevisoes() {
        return ultimasPrevisoes;
    }

    public List<Alerta> getAlertasRecentes() {
        return alertasRecentes;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    /** Texto pronto para exibir no console. */
    public String formatar() {
        StringBuilder texto = new StringBuilder();
        texto.append("========== RELATÓRIO DE STATUS ==========\n");
        texto.append("Região: ").append(territorio.getNomeRegiao()).append('\n');
        texto.append("Gerado em: ").append(geradoEm.format(FORMATO_DATA_HORA)).append('\n');
        texto.append("População estimada: ")
                .append(NumberFormat.getIntegerInstance(PORTUGUES_BRASIL).format(territorio.getPopulacaoEstimada()))
                .append(" | Pontos vulneráveis monitorados: ")
                .append(territorio.getQuantidadePontosVulneraveis()).append('\n');

        texto.append("\n-- Sensores --\n");
        statusSensores.forEach(status -> texto.append("  ").append(status).append('\n'));

        texto.append("\n-- Últimas previsões --\n");
        if (ultimasPrevisoes.isEmpty()) {
            texto.append("  Nenhuma previsão incluída.\n");
        }
        ultimasPrevisoes.forEach((sensor, resultado) -> texto.append("  ").append(sensor).append(" [")
                .append(resultado.estrategia()).append("]: ").append(resultado.interpretacao()).append('\n'));

        texto.append("\n-- Alertas recentes --\n");
        if (alertasRecentes.isEmpty()) {
            texto.append("  Nenhum alerta recente.\n");
        }
        alertasRecentes.forEach(alerta -> texto.append("  ").append(alerta).append('\n'));

        texto.append("=========================================");
        return texto.toString();
    }
}
