package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.EstrategiaPrevisao;
import br.com.fiap.tpi.domain.PrevisaoComparacao;
import br.com.fiap.tpi.domain.PrevisaoMediaMovel;
import br.com.fiap.tpi.domain.TipoPrevisao;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.service.FabricaPrevisao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FabricaPrevisaoTest {

    private final FabricaPrevisao fabrica = new FabricaPrevisao();

    @Test
    void criarMediaMovelDevolvePrevisaoMediaMovel() throws DadosInvalidosException {
        // Arrange
        TipoPrevisao tipo = TipoPrevisao.MEDIA_MOVEL;

        // Act
        EstrategiaPrevisao estrategia = fabrica.criar(tipo);

        // Assert
        assertInstanceOf(PrevisaoMediaMovel.class, estrategia);
    }

    @Test
    void criarComparacaoDevolvePrevisaoComparacao() throws DadosInvalidosException {
        // Arrange
        TipoPrevisao tipo = TipoPrevisao.COMPARACAO;

        // Act
        EstrategiaPrevisao estrategia = fabrica.criar(tipo);

        // Assert
        assertInstanceOf(PrevisaoComparacao.class, estrategia);
    }

    @Test
    void tipoNuloLancaDadosInvalidosException() {
        // Arrange
        TipoPrevisao tipoDesconhecido = null;

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> fabrica.criar(tipoDesconhecido));
    }
}
