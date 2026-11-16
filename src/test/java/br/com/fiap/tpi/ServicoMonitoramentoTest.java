package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.Alerta;
import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.NivelAlerta;
import br.com.fiap.tpi.domain.ResultadoPrevisao;
import br.com.fiap.tpi.domain.SensorPluviometro;
import br.com.fiap.tpi.domain.TipoPrevisao;
import br.com.fiap.tpi.domain.TipoSensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.repository.AlertaRepository;
import br.com.fiap.tpi.repository.MedicaoRepository;
import br.com.fiap.tpi.service.ConfiguracaoSistema;
import br.com.fiap.tpi.service.FabricaPrevisao;
import br.com.fiap.tpi.service.ServicoMonitoramento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa o serviço sem banco: os repositories são mocks do Mockito.
 * Limiares padrão de chuva: Atenção 20, Alerta 40, Crítico 60 mm/h.
 */
@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoRepository medicaoRepository;

    @Mock
    private AlertaRepository alertaRepository;

    private ServicoMonitoramento servico;
    private SensorPluviometro pluviometro;

    @BeforeEach
    void prepararCenario() throws DadosInvalidosException {
        servico = new ServicoMonitoramento(medicaoRepository, alertaRepository,
                new ConfiguracaoSistema(), new FabricaPrevisao());
        pluviometro = new SensorPluviometro("PLU-01", "Pluviômetro do Córrego", "Zona Leste");
    }

    // ---------- Medições ----------

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

    // ---------- Previsão ----------

    @Test
    void rodarPrevisaoUsaHistoricoDoRepositoryEEstrategiaDaFabrica() throws DadosInvalidosException {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 16, 14, 0);
        List<Medicao> historico = new ArrayList<>();
        double[] valores = {10, 20, 30, 40, 50};
        for (int i = 0; i < valores.length; i++) {
            historico.add(Medicao.criar("PLU-01", "Pluviômetro do Córrego", "Zona Leste",
                    TipoSensor.CHUVA, valores[i], inicio.plusMinutes(i)));
        }
        when(medicaoRepository.findBySensorIdOrderByDataHoraAsc("PLU-01")).thenReturn(historico);

        // Act
        ResultadoPrevisao resultado = servico.rodarPrevisao("PLU-01", TipoPrevisao.MEDIA_MOVEL);

        // Assert
        assertEquals("Média móvel", resultado.estrategia());
        assertEquals(30.0, resultado.valorPrevisto(), 0.0001);
        verify(medicaoRepository).findBySensorIdOrderByDataHoraAsc("PLU-01");
    }

    // ---------- Alertas ----------

    @Test
    void previsaoAcimaDoLimiarCriticoGeraAlertaCriticoEGrava() {
        // Arrange
        ResultadoPrevisao previsao = new ResultadoPrevisao(70.0, "Média móvel", 5, true, "teste");

        // Act
        Optional<Alerta> alerta = servico.avaliarAlerta(pluviometro, previsao);

        // Assert
        assertTrue(alerta.isPresent());
        assertEquals(NivelAlerta.CRITICO, alerta.get().getNivel());
        assertEquals(60.0, alerta.get().getLimiteEstourado(), 0.0001);
        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    void previsaoEntreAlertaECriticoGeraNivelAlerta() {
        // Arrange
        ResultadoPrevisao previsao = new ResultadoPrevisao(45.0, "Média móvel", 5, true, "teste");

        // Act
        Optional<Alerta> alerta = servico.avaliarAlerta(pluviometro, previsao);

        // Assert
        assertTrue(alerta.isPresent());
        assertEquals(NivelAlerta.ALERTA, alerta.get().getNivel());
    }

    @Test
    void previsaoAbaixoDeAtencaoNaoGeraAlerta() {
        // Arrange
        ResultadoPrevisao previsao = new ResultadoPrevisao(10.0, "Média móvel", 5, true, "teste");

        // Act
        Optional<Alerta> alerta = servico.avaliarAlerta(pluviometro, previsao);

        // Assert
        assertFalse(alerta.isPresent());
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void previsaoNaoConfiavelNuncaGeraAlerta() {
        // Arrange — valor alto, mas com dados insuficientes
        ResultadoPrevisao previsao = ResultadoPrevisao.insuficiente("Média móvel", 3, 5);

        // Act
        Optional<Alerta> alerta = servico.avaliarAlerta(pluviometro, previsao);

        // Assert
        assertFalse(alerta.isPresent());
        verify(alertaRepository, never()).save(any(Alerta.class));
    }
}
