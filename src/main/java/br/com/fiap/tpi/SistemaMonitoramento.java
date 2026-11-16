package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.PontoMonitoramento;
import br.com.fiap.tpi.domain.Sensor;
import br.com.fiap.tpi.domain.SensorPluviometro;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.exception.FonteIndisponivelException;
import br.com.fiap.tpi.service.ServicoMonitoramento;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Ponto de entrada e menu de console do sistema.
 * Todas as exceções customizadas são capturadas aqui e viram mensagens
 * claras para o usuário — o programa nunca é derrubado por entrada inválida.
 */
@SpringBootApplication
@Profile("!test")
public class SistemaMonitoramento implements CommandLineRunner {

    private final ServicoMonitoramento servico;
    private final List<PontoMonitoramento> pontos = new ArrayList<>();
    private Scanner entrada;

    /** O Spring injeta o serviço pelo construtor. */
    public SistemaMonitoramento(ServicoMonitoramento servico) {
        this.servico = servico;
    }

    public static void main(String[] args) {
        SpringApplication.run(SistemaMonitoramento.class, args);
    }

    @Override
    public void run(String... args) {
        entrada = new Scanner(System.in);
        try {
            criarTerritorioSimulado();
        } catch (DadosInvalidosException e) {
            System.out.println("Erro ao montar o território simulado: " + e.getMessage());
            return;
        }

        boolean continuar = true;
        while (continuar) {
            exibirMenu();
            String opcao = lerLinha("Escolha uma opção: ");
            if (opcao == null) {
                break; // fim da entrada (ex.: Ctrl+D)
            }
            switch (opcao) {
                case "1" -> verificarSensores();
                case "2" -> registrarLeituraManual();
                case "4" -> verHistorico();
                case "0" -> continuar = false;
                default -> System.out.println("Opção inválida: \"" + opcao + "\". Digite um número do menu.");
            }
        }
        System.out.println("Encerrando o sistema. Até logo!");
    }

    private void criarTerritorioSimulado() throws DadosInvalidosException {
        PontoMonitoramento corrego = new PontoMonitoramento(
                "PT-01", "Córrego do Bairro – trecho baixo", "Zona Leste – Bacia do Córrego");
        corrego.adicionarSensor(new SensorPluviometro(
                "PLU-01", "Pluviômetro do Córrego", "Zona Leste – Bacia do Córrego"));

        PontoMonitoramento viaduto = new PontoMonitoramento(
                "PT-02", "Passagem sob o viaduto", "Zona Leste – Bacia do Córrego");
        viaduto.adicionarSensor(new SensorPluviometro(
                "PLU-02", "Pluviômetro do Viaduto", "Zona Leste – Bacia do Córrego"));

        pontos.add(corrego);
        pontos.add(viaduto);
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("=== Territory Predictive Insights — Clima Local ===");
        System.out.println("1. Verificar sensores (ler e gravar medições)");
        System.out.println("2. Registrar leitura manual");
        System.out.println("4. Ver histórico de medições por sensor");
        System.out.println("0. Sair");
    }

    // ---------- Opção 1 ----------

    private void verificarSensores() {
        for (PontoMonitoramento ponto : pontos) {
            System.out.println();
            for (Sensor sensor : ponto.getSensores()) {
                lerUmSensor(sensor);
            }
            System.out.println(ponto.verificarStatus());
        }
    }

    /** Um sensor com falha gera aviso, mas não impede a leitura dos demais. */
    private void lerUmSensor(Sensor sensor) {
        try {
            Medicao medicao = servico.lerSensor(sensor);
            System.out.println("  ✔ Gravada: " + medicao);
        } catch (FonteIndisponivelException e) {
            System.out.println("  ⚠ Aviso: " + e.getMessage() + ". Leitura ignorada.");
        } catch (DadosInvalidosException e) {
            System.out.println("  ✖ Leitura descartada: " + e.getMessage());
        }
    }

    // ---------- Opção 2 ----------

    private void registrarLeituraManual() {
        try {
            String sensorId = lerLinha("ID do sensor (" + listarIdsSensores() + "): ");
            if (sensorId == null) {
                return;
            }
            Sensor sensor = buscarSensorPorId(sensorId);
            String texto = lerLinha("Valor medido (" + sensor.getTipo().getUnidade() + "): ");
            if (texto == null) {
                return;
            }
            Medicao medicao = servico.registrarLeituraManual(sensor, converterDecimal(texto));
            System.out.println("Leitura gravada: " + medicao);
        } catch (DadosInvalidosException e) {
            System.out.println("Não foi possível registrar: " + e.getMessage());
        }
    }

    // ---------- Opção 4 ----------

    private void verHistorico() {
        try {
            String sensorId = lerLinha("ID do sensor (" + listarIdsSensores() + "): ");
            if (sensorId == null) {
                return;
            }
            Sensor sensor = buscarSensorPorId(sensorId);
            List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getId());
            if (historico.isEmpty()) {
                System.out.println("Nenhuma medição gravada para " + sensor.getNome() + ".");
                return;
            }
            System.out.println("Histórico de " + sensor.getNome() + " (" + historico.size() + " medições):");
            historico.forEach(m -> System.out.println("  " + m));
        } catch (DadosInvalidosException e) {
            System.out.println("Não foi possível consultar: " + e.getMessage());
        }
    }

    // ---------- Apoio ----------

    private Sensor buscarSensorPorId(String sensorId) throws DadosInvalidosException {
        for (PontoMonitoramento ponto : pontos) {
            for (Sensor sensor : ponto.getSensores()) {
                if (sensor.getId().equalsIgnoreCase(sensorId)) {
                    return sensor;
                }
            }
        }
        throw new DadosInvalidosException("Sensor \"" + sensorId + "\" não encontrado no território");
    }

    private String listarIdsSensores() {
        List<String> ids = new ArrayList<>();
        for (PontoMonitoramento ponto : pontos) {
            for (Sensor sensor : ponto.getSensores()) {
                ids.add(sensor.getId());
            }
        }
        return String.join(", ", ids);
    }

    /** Aceita vírgula ou ponto como separador decimal. */
    private double converterDecimal(String texto) throws DadosInvalidosException {
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("\"" + texto + "\" não é um número válido", e);
        }
    }

    /** Retorna null quando não há mais entrada, para o menu encerrar sem erro. */
    private String lerLinha(String mensagem) {
        System.out.print(mensagem);
        if (!entrada.hasNextLine()) {
            return null;
        }
        return entrada.nextLine().trim();
    }
}
