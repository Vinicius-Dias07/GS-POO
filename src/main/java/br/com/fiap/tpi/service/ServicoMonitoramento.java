package br.com.fiap.tpi.service;

import br.com.fiap.tpi.domain.Alerta;
import br.com.fiap.tpi.domain.EstrategiaPrevisao;
import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.NivelAlerta;
import br.com.fiap.tpi.domain.ResultadoPrevisao;
import br.com.fiap.tpi.domain.Sensor;
import br.com.fiap.tpi.domain.TipoPrevisao;
import br.com.fiap.tpi.domain.TipoSensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.exception.FonteIndisponivelException;
import br.com.fiap.tpi.repository.AlertaRepository;
import br.com.fiap.tpi.repository.MedicaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Regras de negócio do monitoramento. O menu só conversa com este serviço;
 * o serviço é quem valida, prevê, avalia alertas e grava no banco.
 * As dependências chegam pelo construtor para que os testes possam
 * substituí-las por mocks (Mockito).
 */
@Service
public class ServicoMonitoramento {

    private static final Locale PORTUGUES_BRASIL = Locale.of("pt", "BR");

    private final MedicaoRepository medicaoRepository;
    private final AlertaRepository alertaRepository;
    private final ConfiguracaoSistema configuracao;
    private final FabricaPrevisao fabricaPrevisao;

    public ServicoMonitoramento(MedicaoRepository medicaoRepository,
                                AlertaRepository alertaRepository,
                                ConfiguracaoSistema configuracao,
                                FabricaPrevisao fabricaPrevisao) {
        this.medicaoRepository = medicaoRepository;
        this.alertaRepository = alertaRepository;
        this.configuracao = configuracao;
        this.fabricaPrevisao = fabricaPrevisao;
    }

    // ---------- Medições ----------

    /**
     * Lê um sensor, valida e grava a medição.
     * As exceções sobem para o menu, que decide como avisar o usuário —
     * assim um sensor com falha não impede a leitura dos demais.
     */
    public Medicao lerSensor(Sensor sensor) throws FonteIndisponivelException, DadosInvalidosException {
        double valor = sensor.lerValor();
        return gravarMedicao(sensor, valor);
    }

    /** Registra um valor digitado pelo usuário; valores fora da faixa são rejeitados. */
    public Medicao registrarLeituraManual(Sensor sensor, double valor) throws DadosInvalidosException {
        return gravarMedicao(sensor, valor);
    }

    public List<Medicao> buscarHistoricoPorSensor(String sensorId) {
        return medicaoRepository.findBySensorIdOrderByDataHoraAsc(sensorId);
    }

    // ---------- Previsão e alertas ----------

    /**
     * Busca o histórico do sensor no banco e aplica a estratégia pedida.
     * A Factory entrega a estratégia certa; aqui só conhecemos EstrategiaPrevisao
     * e o polimorfismo decide qual prever() é executado.
     */
    public ResultadoPrevisao rodarPrevisao(String sensorId, TipoPrevisao tipo) throws DadosInvalidosException {
        EstrategiaPrevisao estrategia = fabricaPrevisao.criar(tipo);
        return estrategia.prever(buscarHistoricoPorSensor(sensorId));
    }

    /** Mesma previsão, usando a estratégia padrão configurada. */
    public ResultadoPrevisao rodarPrevisao(String sensorId) throws DadosInvalidosException {
        return rodarPrevisao(sensorId, configuracao.getEstrategiaPadrao());
    }

    /**
     * Compara a previsão com os limiares do tipo de sensor e, se algum for
     * atingido, emite (grava) um alerta com o MAIOR nível estourado.
     * Previsão não confiável nunca gera alerta: o sistema prefere se calar.
     */
    public Optional<Alerta> avaliarAlerta(Sensor sensor, ResultadoPrevisao resultado) {
        if (resultado == null || !resultado.confiavel()) {
            return Optional.empty();
        }
        TipoSensor tipo = sensor.getTipo();
        NivelAlerta[] niveis = NivelAlerta.values();

        // Percorre do mais grave (CRÍTICO) para o mais leve (ATENÇÃO).
        for (int i = niveis.length - 1; i >= 0; i--) {
            NivelAlerta nivel = niveis[i];
            double limite = configuracao.getLimiar(tipo, nivel);
            if (resultado.valorPrevisto() >= limite) {
                Alerta alerta = new Alerta(sensor.getId(), sensor.getRegiao(), tipo, nivel,
                        resultado.valorPrevisto(), limite, resultado.estrategia(),
                        montarMensagem(sensor, nivel, resultado.valorPrevisto(), limite, resultado.estrategia()),
                        LocalDateTime.now());
                alertaRepository.save(alerta);
                return Optional.of(alerta);
            }
        }
        return Optional.empty();
    }

    public List<Alerta> listarAlertasEmitidos() {
        return alertaRepository.findAllByOrderByDataHoraDesc();
    }

    // ---------- Apoio ----------

    private Medicao gravarMedicao(Sensor sensor, double valor) throws DadosInvalidosException {
        Medicao medicao = Medicao.criar(sensor.getId(), sensor.getNome(), sensor.getRegiao(),
                sensor.getTipo(), valor, LocalDateTime.now());
        return medicaoRepository.save(medicao);
    }

    private String montarMensagem(Sensor sensor, NivelAlerta nivel, double valorPrevisto,
                                  double limite, String estrategia) {
        TipoSensor tipo = sensor.getTipo();
        return String.format(PORTUGUES_BRASIL,
                "[%s] %s (%s): previsão de %s de %.1f %s ultrapassa o limite do nível %s (%.1f %s) — estratégia: %s",
                nivel.getDescricao().toUpperCase(PORTUGUES_BRASIL), sensor.getNome(), sensor.getRegiao(),
                tipo.getDescricao(), valorPrevisto, tipo.getUnidade(),
                nivel.getDescricao(), limite, tipo.getUnidade(), estrategia);
    }
}
