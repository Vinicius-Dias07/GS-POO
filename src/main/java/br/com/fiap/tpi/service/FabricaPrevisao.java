package br.com.fiap.tpi.service;

import br.com.fiap.tpi.domain.EstrategiaPrevisao;
import br.com.fiap.tpi.domain.PrevisaoComparacao;
import br.com.fiap.tpi.domain.PrevisaoMediaMovel;
import br.com.fiap.tpi.domain.TipoPrevisao;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Padrão criacional FACTORY.
 * É o único lugar do sistema que sabe qual classe corresponde a cada
 * TipoPrevisao. O resto do código pede "uma estratégia do tipo X" e recebe
 * uma EstrategiaPrevisao, sem if/switch/instanceof espalhados.
 * Para adicionar uma estratégia nova basta uma linha no construtor.
 */
@Component
public class FabricaPrevisao {

    /** Quantas medições cada estratégia considera. */
    public static final int JANELA_PADRAO = 5;

    private final Map<TipoPrevisao, Supplier<EstrategiaPrevisao>> estrategias = new EnumMap<>(TipoPrevisao.class);

    public FabricaPrevisao() {
        estrategias.put(TipoPrevisao.MEDIA_MOVEL, () -> new PrevisaoMediaMovel(JANELA_PADRAO));
        estrategias.put(TipoPrevisao.COMPARACAO, () -> new PrevisaoComparacao(JANELA_PADRAO));
    }

    public EstrategiaPrevisao criar(TipoPrevisao tipo) throws DadosInvalidosException {
        Supplier<EstrategiaPrevisao> fornecedor = tipo == null ? null : estrategias.get(tipo);
        if (fornecedor == null) {
            throw new DadosInvalidosException("Estratégia de previsão desconhecida: " + tipo);
        }
        return fornecedor.get();
    }
}
