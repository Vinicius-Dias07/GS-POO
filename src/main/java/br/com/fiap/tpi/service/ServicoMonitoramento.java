package br.com.fiap.tpi.service;

import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.Sensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.exception.FonteIndisponivelException;
import br.com.fiap.tpi.repository.MedicaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de negócio do monitoramento. O menu só conversa com este serviço;
 * o serviço é quem valida e grava no banco.
 * As dependências chegam pelo construtor para que os testes possam
 * substituí-las por mocks (Mockito).
 */
@Service
public class ServicoMonitoramento {

    private final MedicaoRepository medicaoRepository;

    public ServicoMonitoramento(MedicaoRepository medicaoRepository) {
        this.medicaoRepository = medicaoRepository;
    }

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

    private Medicao gravarMedicao(Sensor sensor, double valor) throws DadosInvalidosException {
        Medicao medicao = Medicao.criar(sensor.getId(), sensor.getNome(), sensor.getRegiao(),
                sensor.getTipo(), valor, LocalDateTime.now());
        return medicaoRepository.save(medicao);
    }
}
