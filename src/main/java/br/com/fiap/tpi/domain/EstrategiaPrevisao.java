package br.com.fiap.tpi.domain;

import java.util.List;
import java.util.Locale;

/**
 * Base abstrata das estratégias de previsão.
 * Concentra a regra comum a todas — "com menos de 5 leituras o sistema
 * não prevê" — e deixa cada filha sobrescrever prever() com seu cálculo,
 * reaproveitando essa regra via super.prever(...).
 */
public abstract class EstrategiaPrevisao {

    protected static final int MINIMO_LEITURAS = 5;
    private static final Locale PORTUGUES_BRASIL = Locale.of("pt", "BR");

    private final String nome;
    private final int janela;

    protected EstrategiaPrevisao(String nome, int janela) {
        if (janela < 1) {
            throw new IllegalArgumentException("A janela da previsão deve ter pelo menos 1 medição");
        }
        this.nome = nome;
        this.janela = janela;
    }

    /**
     * Regra comum: devolve um resultado "insuficiente" quando há menos de
     * {@value #MINIMO_LEITURAS} leituras; devolve null para sinalizar que a
     * classe filha pode calcular.
     */
    public ResultadoPrevisao prever(List<Medicao> historico) {
        int quantidade = historico == null ? 0 : historico.size();
        if (quantidade < MINIMO_LEITURAS) {
            return ResultadoPrevisao.insuficiente(nome, quantidade, MINIMO_LEITURAS);
        }
        return null;
    }

    public String getNome() {
        return nome;
    }

    public int getJanela() {
        return janela;
    }

    // ---------- Ferramentas compartilhadas pelas filhas ----------

    /** As últimas {@code quantidade} medições (ou todas, se houver menos). */
    protected static List<Medicao> ultimas(List<Medicao> historico, int quantidade) {
        int inicio = Math.max(0, historico.size() - quantidade);
        return historico.subList(inicio, historico.size());
    }

    protected static double media(List<Medicao> medicoes) {
        return medicoes.stream().mapToDouble(Medicao::getValor).average().orElse(0.0);
    }

    /** Formata no padrão brasileiro com a unidade: "42,3 mm/h". */
    protected static String formatar(double valor, TipoSensor tipo) {
        return String.format(PORTUGUES_BRASIL, "%.1f %s", valor, tipo.getUnidade());
    }
}
