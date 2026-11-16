package br.com.fiap.tpi.domain;

import java.util.List;

/**
 * Prevê a próxima leitura como a média das últimas N medições.
 * Suaviza picos isolados: uma leitura muito alta sozinha não dispara alerta.
 */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    public PrevisaoMediaMovel(int janela) {
        super("Média móvel", janela);
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) {
        // Reaproveita a regra da classe mãe: com poucos dados, não prevê.
        ResultadoPrevisao resultadoInsuficiente = super.prever(historico);
        if (resultadoInsuficiente != null) {
            return resultadoInsuficiente;
        }

        List<Medicao> recentes = ultimas(historico, getJanela());
        double mediaRecente = media(recentes);
        TipoSensor tipo = historico.get(historico.size() - 1).getTipoSensor();

        String interpretacao = "Previsão de " + tipo.getDescricao() + " de " + formatar(mediaRecente, tipo)
                + " — média das últimas " + recentes.size() + " leituras";
        return new ResultadoPrevisao(mediaRecente, getNome(), historico.size(), true, interpretacao);
    }
}
