package br.com.fiap.tpi.domain;

/**
 * Resultado imutável de uma previsão (record do Java).
 *
 * @param valorPrevisto      valor projetado para a próxima leitura
 * @param estrategia         nome da estratégia usada
 * @param quantidadeLeituras quantas medições havia no histórico
 * @param confiavel          false quando o sistema decidiu não prever
 * @param interpretacao      texto explicando o que o número significa
 */
public record ResultadoPrevisao(double valorPrevisto,
                                String estrategia,
                                int quantidadeLeituras,
                                boolean confiavel,
                                String interpretacao) {

    /** Resultado usado quando não há leituras suficientes: o sistema "se cala". */
    public static ResultadoPrevisao insuficiente(String estrategia, int quantidadeLeituras, int minimoNecessario) {
        return new ResultadoPrevisao(0.0, estrategia, quantidadeLeituras, false,
                "Dados insuficientes — o sistema prefere não prever ("
                        + quantidadeLeituras + " de " + minimoNecessario + " leituras necessárias)");
    }
}
