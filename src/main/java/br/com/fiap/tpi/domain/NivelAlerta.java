package br.com.fiap.tpi.domain;

/**
 * Níveis de alerta, do menos para o mais grave.
 * A ordem de declaração importa: é usada para escolher o maior nível estourado.
 */
public enum NivelAlerta {

    ATENCAO("Atenção"),
    ALERTA("Alerta"),
    CRITICO("Crítico");

    private final String descricao;

    NivelAlerta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
