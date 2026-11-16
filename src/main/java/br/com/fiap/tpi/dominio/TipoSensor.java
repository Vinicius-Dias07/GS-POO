package br.com.fiap.tpi.dominio;

/**
 * Tipos de sensor do território. Cada tipo conhece sua unidade e a faixa
 * de valores fisicamente válida, usada para validar as medições.
 */
public enum TipoSensor {

    CHUVA("chuva", "mm/h", 0, 200),
    NIVEL_RIO("nível do rio", "m", 0, 10),
    UMIDADE_SOLO("umidade do solo", "%", 0, 100);

    private final String descricao;
    private final String unidade;
    private final double minimoValido;
    private final double maximoValido;

    TipoSensor(String descricao, String unidade, double minimoValido, double maximoValido) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.minimoValido = minimoValido;
        this.maximoValido = maximoValido;
    }

    public boolean estaNaFaixaValida(double valor) {
        return valor >= minimoValido && valor <= maximoValido;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getMinimoValido() {
        return minimoValido;
    }

    public double getMaximoValido() {
        return maximoValido;
    }
}
