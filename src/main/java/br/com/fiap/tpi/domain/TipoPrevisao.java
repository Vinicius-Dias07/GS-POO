package br.com.fiap.tpi.domain;

/**
 * Estratégias de previsão disponíveis. Quem cria o objeto certo para cada
 * tipo é a FabricaPrevisao — nenhum outro lugar faz if/switch por tipo.
 */
public enum TipoPrevisao {

    MEDIA_MOVEL("Média móvel"),
    COMPARACAO("Comparação com período anterior");

    private final String descricao;

    TipoPrevisao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
