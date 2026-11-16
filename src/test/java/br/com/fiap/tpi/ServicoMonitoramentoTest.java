package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.SensorPluviometro;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.repository.MedicaoRepository;
import br.com.fiap.tpi.service.ServicoMonitoramento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa o serviço sem banco: o repository é um mock do Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoRepository medicaoRepository;

    private ServicoMonitoramento servico;
    private SensorPluviometro pluviometro;

    @BeforeEach
    void prepararCenario() throws DadosInvalidosException {
        servico = new ServicoMonitoramento(medicaoRepository);
        pluviometro = new SensorPluviometro("PLU-01", "Pluviômetro do Córrego", "Zona Leste");
    }

    @Test
    void leituraManualValidaEGravadaNoRepository() throws DadosInvalidosException {
        // Arrange
        when(medicaoRepository.save(any(Medicao.class))).thenAnswer(chamada -> chamada.getArgument(0));

        // Act
        Medicao gravada = servico.registrarLeituraManual(pluviometro, 35.0);

        // Assert
        assertEquals(35.0, gravada.getValor(), 0.0001);
        verify(medicaoRepository).save(any(Medicao.class));
    }

    @Test
    void leituraManualInvalidaNaoEGravada() {
        // Arrange
        double chuvaNegativa = -5.0;

        // Act
        assertThrows(DadosInvalidosException.class,
                () -> servico.registrarLeituraManual(pluviometro, chuvaNegativa));

        // Assert
        verify(medicaoRepository, never()).save(any(Medicao.class));
    }
}
