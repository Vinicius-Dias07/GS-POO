package br.com.fiap.tpi.dominio;

import br.com.fiap.tpi.excecao.DadosInvalidosException;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Uma leitura de sensor, gravada no banco (entidade JPA).
 * Só é criada pelo método fábrica {@link #criar}, que valida o valor
 * contra a faixa do tipo de sensor — assim nunca existe medição inválida.
 */
@Entity
public class Medicao {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sensorId;
    private String nomeSensor;
    private String regiao;

    @Enumerated(EnumType.STRING)
    private TipoSensor tipoSensor;

    private double valor;
    private LocalDateTime dataHora;

    /** Exigido pelo JPA; não deve ser usado pelo código da aplicação. */
    protected Medicao() {
    }

    private Medicao(String sensorId, String nomeSensor, String regiao,
                    TipoSensor tipoSensor, double valor, LocalDateTime dataHora) {
        this.sensorId = sensorId;
        this.nomeSensor = nomeSensor;
        this.regiao = regiao;
        this.tipoSensor = tipoSensor;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public static Medicao criar(String sensorId, String nomeSensor, String regiao,
                                TipoSensor tipoSensor, double valor, LocalDateTime dataHora)
            throws DadosInvalidosException {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("A medição precisa informar o sensor de origem");
        }
        if (tipoSensor == null) {
            throw new DadosInvalidosException("A medição do sensor " + sensorId + " precisa informar o tipo");
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("A medição do sensor " + sensorId + " precisa informar data e hora");
        }
        if (valor < tipoSensor.getMinimoValido()) {
            throw new DadosInvalidosException("Medição de " + tipoSensor.getDescricao() + " negativa: "
                    + valor + " " + tipoSensor.getUnidade());
        }
        if (valor > tipoSensor.getMaximoValido()) {
            throw new DadosInvalidosException("Medição de " + tipoSensor.getDescricao()
                    + " acima do máximo válido (" + tipoSensor.getMaximoValido() + " "
                    + tipoSensor.getUnidade() + "): " + valor + " " + tipoSensor.getUnidade());
        }
        return new Medicao(sensorId, nomeSensor, regiao, tipoSensor, valor, dataHora);
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getNomeSensor() {
        return nomeSensor;
    }

    public String getRegiao() {
        return regiao;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return dataHora.format(FORMATO_DATA_HORA) + " | " + nomeSensor + " (" + regiao + ") | "
                + valor + " " + tipoSensor.getUnidade();
    }
}
