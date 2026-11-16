package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.PrevisaoComparacao;
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

class PrevisaoComparacaoTest {

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
    void tendenciaDeAltaProjetaValorMaiorQueAMediaRecente() throws DadosInvalidosException {
        // Arrange — período anterior com média 10; recente com média 40; último valor 60
        List<Medicao> historico = historicoDeChuva(10, 10, 10, 10, 10, 20, 30, 40, 50, 60);
        PrevisaoComparacao estrategia = new PrevisaoComparacao(5);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert — projeção = 60 + (40 − 10) = 90
        assertTrue(resultado.confiavel());
        assertEquals(90.0, resultado.valorPrevisto(), 0.0001);
        assertTrue(resultado.valorPrevisto() > 40.0);
        assertTrue(resultado.interpretacao().contains("tendência de alta"));
    }

    @Test
    void tendenciaDeQuedaNuncaPreveChuvaNegativa() throws DadosInvalidosException {
        // Arrange — chuva diminuindo rápido: média anterior 50, recente 4, último valor 0
        List<Medicao> historico = historicoDeChuva(50, 50, 50, 50, 50, 5, 5, 5, 5, 0);
        PrevisaoComparacao estrategia = new PrevisaoComparacao(5);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert — 0 + (4 − 50) seria negativo; a projeção fica no mínimo físico
        assertEquals(0.0, resultado.valorPrevisto(), 0.0001);
        assertTrue(resultado.interpretacao().contains("tendência de queda"));
    }

    @Test
    void menosDeCincoLeiturasResultaNaoConfiavel() throws DadosInvalidosException {
        // Arrange
        List<Medicao> historico = historicoDeChuva(10, 20, 30);
        PrevisaoComparacao estrategia = new PrevisaoComparacao(5);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertFalse(resultado.confiavel());
    }
}
