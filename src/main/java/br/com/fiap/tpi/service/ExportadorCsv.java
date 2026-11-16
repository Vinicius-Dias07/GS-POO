package br.com.fiap.tpi.service;

import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Bônus: exporta o histórico de medições de um sensor para um arquivo CSV
 * (separado por ponto e vírgula, que abre direto no Excel/LibreOffice em pt-BR).
 */
@Component
public class ExportadorCsv {

    static final String CABECALHO = "id;sensor;regiao;tipo;valor;unidade;dataHora";

    private static final DateTimeFormatter FORMATO_NOME_ARQUIVO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Exporta para a pasta em que o programa foi iniciado. */
    public Path exportar(String sensorId, List<Medicao> historico) throws IOException, DadosInvalidosException {
        return exportar(sensorId, historico, Path.of("."));
    }

    /**
     * Gera historico_&lt;sensorId&gt;_&lt;data&gt;.csv na pasta informada.
     * O try-with-resources garante que o arquivo é fechado mesmo se der erro
     * no meio da escrita. A IOException sobe para o menu avisar o usuário.
     */
    public Path exportar(String sensorId, List<Medicao> historico, Path pasta)
            throws IOException, DadosInvalidosException {
        if (historico == null || historico.isEmpty()) {
            throw new DadosInvalidosException("O sensor " + sensorId + " não tem medições para exportar");
        }
        String nomeArquivo = "historico_" + sensorId + "_" + LocalDateTime.now().format(FORMATO_NOME_ARQUIVO) + ".csv";
        Path arquivo = pasta.resolve(nomeArquivo);

        try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            escritor.write(CABECALHO);
            escritor.newLine();
            for (Medicao medicao : historico) {
                escritor.write(montarLinha(medicao));
                escritor.newLine();
            }
        }
        return arquivo;
    }

    private static String montarLinha(Medicao medicao) {
        return String.join(";",
                medicao.getId() == null ? "" : String.valueOf(medicao.getId()),
                campo(medicao.getNomeSensor()),
                campo(medicao.getRegiao()),
                medicao.getTipoSensor().name(),
                String.valueOf(medicao.getValor()).replace('.', ','), // vírgula decimal (pt-BR)
                medicao.getTipoSensor().getUnidade(),
                medicao.getDataHora().format(FORMATO_DATA_HORA));
    }

    /** Coloca o texto entre aspas se ele tiver ; ou aspas, para não quebrar as colunas. */
    private static String campo(String texto) {
        if (texto == null) {
            return "";
        }
        if (texto.contains(";") || texto.contains("\"")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }
}
