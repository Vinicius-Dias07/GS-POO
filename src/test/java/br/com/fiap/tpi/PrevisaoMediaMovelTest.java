package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.PrevisaoMediaMovel;
import br.com.fiap.tpi.domain.ResultadoPrevisao;
import br.com.fiap.tpi.domain.TipoSensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoMediaMovelTest {

    /** Monta um histórico de chuva com os valores dados, um por minuto. */
    private static List<Medicao> historicoDeChuva(double... valores) throws DadosInvalidosException {
        List<Medicao> historico = new ArrayList<>();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 16, 14, 0);
        for (int i = 0; i < valores.length; i++) {
            historico.add(Medicao.criar("PLU-01", "Pluviômetro do Córrego", "Zona Leste",
                    TipoSensor.CHUVA, valores[i], inicio.plusMinutes(i)));
        }
        return historico;
    }

    @Test
    void mediaDasCincoUltimasLeituras() throws DadosInvalidosException {
        // Arrange
        List<Medicao> historico = historicoDeChuva(10, 20, 30, 40, 50);
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(5);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertTrue(resultado.confiavel());
        assertEquals(30.0, resultado.valorPrevisto(), 0.0001);
    }

    @Test
    void usaApenasAsUltimasLeiturasDaJanela() throws DadosInvalidosException {
        // Arrange — as duas primeiras leituras ficam fora da janela de 5
        List<Medicao> historico = historicoDeChuva(100, 100, 10, 20, 30, 40, 50);
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(5);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(30.0, resultado.valorPrevisto(), 0.0001);
        assertEquals(7, resultado.quantidadeLeituras());
    }

    @Test
    void menosDeCincoLeiturasResultaNaoConfiavel() throws DadosInvalidosException {
        // Arrange
        List<Medicao> historico = historicoDeChuva(10, 20, 30, 40);
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(5);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertFalse(resultado.confiavel());
        assertTrue(resultado.interpretacao().startsWith("Dados insuficientes"));
    }
}
