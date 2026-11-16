package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.TipoSensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.service.ExportadorCsv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportadorCsvTest {

    private final ExportadorCsv exportador = new ExportadorCsv();

    @Test
    void exportaCabecalhoEUmaLinhaPorMedicao(@TempDir Path pasta) throws Exception {
        // Arrange
        LocalDateTime hora = LocalDateTime.of(2026, 11, 16, 14, 0);
        List<Medicao> historico = List.of(
                Medicao.criar("PLU-01", "Pluviômetro do Córrego", "Zona Leste", TipoSensor.CHUVA, 12.5, hora),
                Medicao.criar("PLU-01", "Pluviômetro do Córrego", "Zona Leste", TipoSensor.CHUVA, 30.0, hora.plusMinutes(1)));

        // Act
        Path arquivo = exportador.exportar("PLU-01", historico, pasta);
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);

        // Assert
        assertTrue(arquivo.getFileName().toString().startsWith("historico_PLU-01_"));
        assertEquals(3, linhas.size());
        assertEquals("id;sensor;regiao;tipo;valor;unidade;dataHora", linhas.get(0));
        assertEquals(";Pluviômetro do Córrego;Zona Leste;CHUVA;12,5;mm/h;16/11/2026 14:00:00", linhas.get(1));
    }

    @Test
    void historicoVazioLancaDadosInvalidosException(@TempDir Path pasta) throws IOException {
        // Arrange
        List<Medicao> historicoVazio = List.of();

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> exportador.exportar("PLU-01", historicoVazio, pasta));
        try (var arquivos = Files.list(pasta)) {
            assertEquals(0, arquivos.count()); // nenhum arquivo foi criado
        }
    }
}
