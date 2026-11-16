package br.com.fiap.tpi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

/**
 * Ponto de entrada do sistema. Nesta fase o menu apenas confirma que a
 * aplicação sobe; as opções reais entram nas próximas fases.
 */
@SpringBootApplication
@Profile("!test")
public class SistemaMonitoramento implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SistemaMonitoramento.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Territory Predictive Insights — Clima Local ===");
        System.out.println("Sistema iniciado. O menu será implementado nas próximas fases.");
    }
}
