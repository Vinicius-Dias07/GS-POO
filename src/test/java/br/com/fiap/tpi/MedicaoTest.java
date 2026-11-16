package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.TipoSensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MedicaoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 14, 0);

    @Test
    void medicaoDeChuvaNegativaLancaDadosInvalidosException() {
        // Arrange
        double valorNegativo = -3.0;

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> Medicao.criar("PLU-01", "Pluviômetro Córrego", "Zona Leste",
                        TipoSensor.CHUVA, valorNegativo, AGORA));

        // Assert
        assertEquals("Medição de chuva negativa: -3.0 mm/h", erro.getMessage());
    }

    @Test
    void medicaoAcimaDoMaximoValidoLancaDadosInvalidosException() {
        // Arrange
        double nivelImpossivel = 15.0; // a faixa válida do nível do rio vai até 10 m

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> Medicao.criar("NIV-01", "Régua do Córrego", "Zona Leste",
                        TipoSensor.NIVEL_RIO, nivelImpossivel, AGORA));
    }

    @Test
    void medicaoValidaMantemValorETipo() throws DadosInvalidosException {
        // Arrange
        double valorChuva = 42.5;

        // Act
        Medicao medicao = Medicao.criar("PLU-01", "Pluviômetro Córrego", "Zona Leste",
                TipoSensor.CHUVA, valorChuva, AGORA);

        // Assert
        assertEquals(42.5, medicao.getValor(), 0.0001);
        assertEquals(TipoSensor.CHUVA, medicao.getTipoSensor());
        assertEquals("PLU-01", medicao.getSensorId());
    }
}
