package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.DadosTerritorio;
import br.com.fiap.tpi.domain.NivelAlerta;
import br.com.fiap.tpi.domain.TipoSensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.service.ConfiguracaoSistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DadosTerritorioTest {

    private static final String CODIGO_CORRETO = "TPI-2026";

    private ConfiguracaoSistema configuracao;
    private DadosTerritorio territorio;

    @BeforeEach
    void prepararCenario() throws DadosInvalidosException {
        configuracao = new ConfiguracaoSistema();
        territorio = new DadosTerritorio("Zona Leste", 48_000, 2, CODIGO_CORRETO, configuracao);
    }

    @Test
    void codigoDeAcessoErradoLancaDadosInvalidosException() {
        // Arrange
        String codigoErrado = "senha-qualquer";

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> territorio.alterarLimiar(codigoErrado, TipoSensor.CHUVA, NivelAlerta.ATENCAO, 25.0));

        // Assert — e o limiar continua o mesmo
        assertEquals("Código de acesso inválido", erro.getMessage());
        assertEquals(20.0, configuracao.getLimiar(TipoSensor.CHUVA, NivelAlerta.ATENCAO), 0.0001);
    }

    @Test
    void codigoCorretoAlteraOLimiar() throws DadosInvalidosException {
        // Arrange
        double novoLimiar = 25.0;

        // Act
        territorio.alterarLimiar(CODIGO_CORRETO, TipoSensor.CHUVA, NivelAlerta.ATENCAO, novoLimiar);

        // Assert
        assertEquals(25.0, configuracao.getLimiar(TipoSensor.CHUVA, NivelAlerta.ATENCAO), 0.0001);
    }

    @Test
    void limiarDeAtencaoMaiorQueAlertaEhIncoerente() {
        // Arrange — o limiar de Alerta da chuva é 40
        double atencaoAcimaDoAlerta = 45.0;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> territorio.alterarLimiar(CODIGO_CORRETO, TipoSensor.CHUVA, NivelAlerta.ATENCAO,
                        atencaoAcimaDoAlerta));
    }

    @Test
    void populacaoNegativaLancaDadosInvalidosException() {
        // Arrange
        int populacaoNegativa = -10;

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> territorio.setPopulacaoEstimada(populacaoNegativa));
    }
}
