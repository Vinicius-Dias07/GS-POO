package br.com.fiap.tpi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Alerta emitido quando uma previsão ultrapassa um limiar. Gravado no banco
 * para que a defesa civil / associação de bairro consulte o histórico.
 */
@Entity
public class Alerta {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sensorId;
    private String regiao;

    @Enumerated(EnumType.STRING)
    private TipoSensor tipoSensor;

    @Enumerated(EnumType.STRING)
    private NivelAlerta nivel;

    private double valorPrevisto;
    private double limiteEstourado;
    private String estrategiaUsada;

    @Column(length = 500)
    private String mensagem;

    private LocalDateTime dataHora;

    /** Exigido pelo JPA; não deve ser usado pelo código da aplicação. */
    protected Alerta() {
    }

    public Alerta(String sensorId, String regiao, TipoSensor tipoSensor, NivelAlerta nivel,
                  double valorPrevisto, double limiteEstourado, String estrategiaUsada,
                  String mensagem, LocalDateTime dataHora) {
        this.sensorId = sensorId;
        this.regiao = regiao;
        this.tipoSensor = tipoSensor;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limiteEstourado = limiteEstourado;
        this.estrategiaUsada = estrategiaUsada;
        this.mensagem = mensagem;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getRegiao() {
        return regiao;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimiteEstourado() {
        return limiteEstourado;
    }

    public String getEstrategiaUsada() {
        return estrategiaUsada;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return dataHora.format(FORMATO_DATA_HORA) + " | " + mensagem;
    }
}
