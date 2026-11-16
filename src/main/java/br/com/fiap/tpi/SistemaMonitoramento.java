package br.com.fiap.tpi;

import br.com.fiap.tpi.dominio.Medicao;
import br.com.fiap.tpi.dominio.PontoMonitoramento;
import br.com.fiap.tpi.dominio.Sensor;
import br.com.fiap.tpi.dominio.SensorPluviometro;
import br.com.fiap.tpi.excecao.DadosInvalidosException;
import br.com.fiap.tpi.excecao.FonteIndisponivelException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
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

    private final List<PontoMonitoramento> pontos = new ArrayList<>();
    // Fase 3: medições ficam só em memória. Na Fase 4 passam a ser gravadas no banco.
    private final List<Medicao> medicoesEmMemoria = new ArrayList<>();
    private Scanner entrada;

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
        System.out.println("1. Verificar sensores (ler medições)");
        System.out.println("2. Registrar leitura manual");
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
            double valor = sensor.lerValor();
            Medicao medicao = criarMedicao(sensor, valor);
            medicoesEmMemoria.add(medicao);
            System.out.println("  ✔ " + medicao);
        } catch (FonteIndisponivelException e) {
            System.out.println("  ⚠ Aviso: " + e.getMessage() + ". Leitura ignorada.");
        } catch (DadosInvalidosException e) {
            System.out.println("  ✖ Leitura descartada: " + e.getMessage());
        }
    }

    // ---------- Opção 2 ----------

    private void registrarLeituraManual() {
        try {
            String sensorId = lerLinha("ID do sensor (ex.: PLU-01): ");
            if (sensorId == null) {
                return;
            }
            Sensor sensor = buscarSensorPorId(sensorId.trim());
            String texto = lerLinha("Valor medido (" + sensor.getTipo().getUnidade() + "): ");
            if (texto == null) {
                return;
            }
            double valor = converterDecimal(texto);
            Medicao medicao = criarMedicao(sensor, valor);
            medicoesEmMemoria.add(medicao);
            System.out.println("Leitura registrada: " + medicao);
        } catch (DadosInvalidosException e) {
            System.out.println("Não foi possível registrar: " + e.getMessage());
        }
    }

    // ---------- Apoio ----------

    private Medicao criarMedicao(Sensor sensor, double valor) throws DadosInvalidosException {
        return Medicao.criar(sensor.getId(), sensor.getNome(), sensor.getRegiao(),
                sensor.getTipo(), valor, LocalDateTime.now());
    }

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

    /** Aceita vírgula ou ponto como separador decimal. */
    private double converterDecimal(String texto) throws DadosInvalidosException {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("\"" + texto.trim() + "\" não é um número válido", e);
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
