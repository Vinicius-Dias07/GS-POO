package br.com.fiap.tpi.domain;

import java.util.List;

/**
 * Compara a média das últimas N leituras com a das N anteriores e projeta a
 * tendência: previsão = último valor + (média recente − média anterior).
 * Antecipa uma chuva que está aumentando, coisa que a média móvel demora a perceber.
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    /** Diferença abaixo da qual consideramos a tendência estável. */
    private static final double MARGEM_ESTABILIDADE = 0.05;

    public PrevisaoComparacao(int janela) {
        super("Comparação com período anterior", janela);
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) {
        // Reaproveita a regra da classe mãe: com poucos dados, não prevê.
        ResultadoPrevisao resultadoInsuficiente = super.prever(historico);
        if (resultadoInsuficiente != null) {
            return resultadoInsuficiente;
        }

        // Se não houver 2N leituras, divide o que existe em duas metades iguais.
        int tamanhoPeriodo = Math.min(getJanela(), historico.size() / 2);
        List<Medicao> recentes = ultimas(historico, tamanhoPeriodo);
        List<Medicao> anteriores = historico.subList(
                historico.size() - 2 * tamanhoPeriodo, historico.size() - tamanhoPeriodo);

        double mediaRecente = media(recentes);
        double mediaAnterior = media(anteriores);
        double variacao = mediaRecente - mediaAnterior;

        Medicao ultima = historico.get(historico.size() - 1);
        TipoSensor tipo = ultima.getTipoSensor();
        // A projeção não pode sair da faixa física do sensor (ex.: chuva negativa).
        double projecao = Math.max(tipo.getMinimoValido(),
                Math.min(tipo.getMaximoValido(), ultima.getValor() + variacao));

        String interpretacao = "Previsão de " + tipo.getDescricao() + " de " + formatar(projecao, tipo)
                + " — " + descreverTendencia(variacao) + " em relação ao período anterior"
                + " (média recente " + formatar(mediaRecente, tipo)
                + " × anterior " + formatar(mediaAnterior, tipo) + ")";
        return new ResultadoPrevisao(projecao, getNome(), historico.size(), true, interpretacao);
    }

    private static String descreverTendencia(double variacao) {
        if (Math.abs(variacao) < MARGEM_ESTABILIDADE) {
            return "tendência estável";
        }
        return variacao > 0 ? "tendência de alta" : "tendência de queda";
    }
}
