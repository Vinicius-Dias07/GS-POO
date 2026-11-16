package br.com.fiap.tpi;

import br.com.fiap.tpi.domain.Alerta;
import br.com.fiap.tpi.domain.DadosTerritorio;
import br.com.fiap.tpi.domain.Medicao;
import br.com.fiap.tpi.domain.NivelAlerta;
import br.com.fiap.tpi.domain.PontoMonitoramento;
import br.com.fiap.tpi.domain.Relatorio;
import br.com.fiap.tpi.domain.ResultadoPrevisao;
import br.com.fiap.tpi.domain.Sensor;
import br.com.fiap.tpi.domain.SensorNivelRio;
import br.com.fiap.tpi.domain.SensorPluviometro;
import br.com.fiap.tpi.domain.SensorUmidadeSolo;
import br.com.fiap.tpi.domain.TipoPrevisao;
import br.com.fiap.tpi.domain.TipoSensor;
import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.exception.FonteIndisponivelException;
import br.com.fiap.tpi.service.ConfiguracaoSistema;
import br.com.fiap.tpi.service.ExportadorCsv;
import br.com.fiap.tpi.service.RelatorioBuilder;
import br.com.fiap.tpi.service.ServicoMonitoramento;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Ponto de entrada e menu de console do sistema.
 * Todas as exceções customizadas são capturadas aqui e viram mensagens
 * claras para o usuário — o programa nunca é derrubado por entrada inválida.
 */
@SpringBootApplication
@Profile("!test")
public class SistemaMonitoramento implements CommandLineRunner {

    /** Código fictício para alterar limiares/estratégia (documentado no README). */
    private static final String CODIGO_ACESSO = "TPI-2026";
    private static final String REGIAO = "Zona Leste – Bacia do Córrego";
    private static final int QUANTIDADE_ALERTAS_NO_RELATORIO = 5;
    private static final Locale PORTUGUES_BRASIL = Locale.of("pt", "BR");

    private final ServicoMonitoramento servico;
    private final ConfiguracaoSistema configuracao;
    private final ExportadorCsv exportadorCsv;
    private final List<PontoMonitoramento> pontos = new ArrayList<>();
    private DadosTerritorio territorio;
    private Scanner entrada;

    /** O Spring injeta as dependências pelo construtor. */
    public SistemaMonitoramento(ServicoMonitoramento servico, ConfiguracaoSistema configuracao,
                                ExportadorCsv exportadorCsv) {
        this.servico = servico;
        this.configuracao = configuracao;
        this.exportadorCsv = exportadorCsv;
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
                case "3" -> rodarPrevisoesEAlertas();
                case "4" -> verHistorico();
                case "5" -> gerenciarTerritorio();
                case "6" -> exibirStatusCompleto();
                case "7" -> exportarHistoricoCsv();
                case "0" -> continuar = false;
                default -> System.out.println("Opção inválida: \"" + opcao + "\". Digite um número do menu.");
            }
        }
        System.out.println("Encerrando o sistema. Até logo!");
    }

    /** Território fictício: descrito por região e equipamento, nunca por pessoas. */
    private void criarTerritorioSimulado() throws DadosInvalidosException {
        PontoMonitoramento corrego = new PontoMonitoramento("PT-01", "Córrego do Bairro – trecho baixo", REGIAO);
        corrego.adicionarSensor(new SensorPluviometro("PLU-01", "Pluviômetro do Córrego", REGIAO));
        corrego.adicionarSensor(new SensorNivelRio("NIV-01", "Régua do Córrego", REGIAO));

        PontoMonitoramento viaduto = new PontoMonitoramento("PT-02", "Passagem sob o viaduto", REGIAO);
        viaduto.adicionarSensor(new SensorPluviometro("PLU-02", "Pluviômetro do Viaduto", REGIAO));
        viaduto.adicionarSensor(new SensorUmidadeSolo("UMI-01", "Umidade do Talude", REGIAO));

        pontos.add(corrego);
        pontos.add(viaduto);
        territorio = new DadosTerritorio(REGIAO, 48_000, pontos.size(), CODIGO_ACESSO, configuracao);
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("=== Territory Predictive Insights — Clima Local ===");
        System.out.println("1. Verificar sensores (ler e gravar medições)");
        System.out.println("2. Registrar leitura manual");
        System.out.println("3. Rodar previsões e alertas");
        System.out.println("4. Ver histórico (medições por sensor / alertas emitidos)");
        System.out.println("5. Gerenciar dados do território (limiares, população, estratégia padrão)");
        System.out.println("6. Status completo (relatório)");
        System.out.println("7. Exportar histórico para CSV  [bônus]");
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
            Sensor sensor = buscarSensorPorId(lerObrigatorio("ID do sensor (" + listarIdsSensores() + "): "));
            String texto = lerObrigatorio("Valor medido (" + sensor.getTipo().getUnidade() + "): ");
            Medicao medicao = servico.registrarLeituraManual(sensor, converterDecimal(texto));
            System.out.println("Leitura gravada: " + medicao);
        } catch (DadosInvalidosException e) {
            System.out.println("Não foi possível registrar: " + e.getMessage());
        }
    }

    // ---------- Opção 3 ----------

    private void rodarPrevisoesEAlertas() {
        try {
            TipoPrevisao tipo = escolherEstrategia();
            for (PontoMonitoramento ponto : pontos) {
                for (Sensor sensor : ponto.getSensores()) {
                    preverEAvaliar(sensor, ponto, tipo);
                }
            }
            System.out.println();
            System.out.println("Os alertas apoiam a decisão da equipe local — não substituem a avaliação humana.");
        } catch (DadosInvalidosException e) {
            System.out.println("Não foi possível rodar as previsões: " + e.getMessage());
        }
    }

    private void preverEAvaliar(Sensor sensor, PontoMonitoramento ponto, TipoPrevisao tipo)
            throws DadosInvalidosException {
        ResultadoPrevisao resultado = servico.rodarPrevisao(sensor.getId(), tipo);
        System.out.println();
        System.out.println(sensor.getNome() + " — " + ponto.getNome());
        System.out.println("  Estratégia: " + resultado.estrategia()
                + " | leituras no histórico: " + resultado.quantidadeLeituras());
        System.out.println("  " + resultado.interpretacao());

        if (!resultado.confiavel()) {
            return; // sem previsão confiável, não há o que avaliar
        }
        Optional<Alerta> alerta = servico.avaliarAlerta(sensor, resultado);
        if (alerta.isPresent()) {
            System.out.println("  🚨 " + alerta.get().getMensagem());
        } else {
            System.out.println("  ✔ Sem alerta: previsão abaixo do nível de atenção.");
        }
    }

    /** Enter vazio usa a estratégia padrão configurada. */
    private TipoPrevisao escolherEstrategia() throws DadosInvalidosException {
        TipoPrevisao padrao = configuracao.getEstrategiaPadrao();
        System.out.println("Estratégia de previsão (Enter = padrão: " + padrao.getDescricao() + ")");
        listarOpcoes(TipoPrevisao.values(), TipoPrevisao::getDescricao);
        String texto = lerObrigatorioOuVazio("Escolha: ");
        return texto.isEmpty() ? padrao : escolherPorNumero(texto, TipoPrevisao.values());
    }

    // ---------- Opção 4 ----------

    private void verHistorico() {
        String escolha = lerLinha("1 - Medições por sensor | 2 - Alertas emitidos: ");
        if (escolha == null) {
            return;
        }
        switch (escolha) {
            case "1" -> verHistoricoMedicoes();
            case "2" -> verAlertasEmitidos();
            default -> System.out.println("Opção inválida: \"" + escolha + "\". Digite 1 ou 2.");
        }
    }

    private void verHistoricoMedicoes() {
        try {
            Sensor sensor = buscarSensorPorId(lerObrigatorio("ID do sensor (" + listarIdsSensores() + "): "));
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

    private void verAlertasEmitidos() {
        List<Alerta> alertas = servico.listarAlertasEmitidos();
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta emitido até agora.");
            return;
        }
        System.out.println("Alertas emitidos (" + alertas.size() + "), do mais recente ao mais antigo:");
        alertas.forEach(a -> System.out.println("  " + a));
    }

    // ---------- Opção 5 ----------

    private void gerenciarTerritorio() {
        System.out.println("1 - Ver limiares atuais");
        System.out.println("2 - Alterar limiar de alerta (exige código de acesso)");
        System.out.println("3 - Alterar população estimada");
        System.out.println("4 - Alterar estratégia de previsão padrão (exige código de acesso)");
        String escolha = lerLinha("Escolha: ");
        if (escolha == null) {
            return;
        }
        try {
            switch (escolha) {
                case "1" -> exibirLimiares();
                case "2" -> alterarLimiar();
                case "3" -> alterarPopulacao();
                case "4" -> alterarEstrategiaPadrao();
                default -> System.out.println("Opção inválida: \"" + escolha + "\". Digite de 1 a 4.");
            }
        } catch (DadosInvalidosException e) {
            System.out.println("Alteração recusada: " + e.getMessage());
        }
    }

    private void exibirLimiares() {
        System.out.println("Limiares atuais (Atenção / Alerta / Crítico):");
        for (TipoSensor tipo : TipoSensor.values()) {
            System.out.println(String.format(PORTUGUES_BRASIL, "  %-16s %6.1f / %6.1f / %6.1f %s",
                    tipo.getDescricao(),
                    configuracao.getLimiar(tipo, NivelAlerta.ATENCAO),
                    configuracao.getLimiar(tipo, NivelAlerta.ALERTA),
                    configuracao.getLimiar(tipo, NivelAlerta.CRITICO),
                    tipo.getUnidade()));
        }
        System.out.println("Estratégia padrão: " + configuracao.getEstrategiaPadrao().getDescricao());
    }

    private void alterarLimiar() throws DadosInvalidosException {
        String codigo = lerObrigatorio("Código de acesso: ");
        System.out.println("Tipo de sensor:");
        listarOpcoes(TipoSensor.values(), TipoSensor::getDescricao);
        TipoSensor tipo = escolherPorNumero(lerObrigatorio("Escolha: "), TipoSensor.values());
        System.out.println("Nível de alerta:");
        listarOpcoes(NivelAlerta.values(), NivelAlerta::getDescricao);
        NivelAlerta nivel = escolherPorNumero(lerObrigatorio("Escolha: "), NivelAlerta.values());
        double valor = converterDecimal(lerObrigatorio("Novo limiar (" + tipo.getUnidade() + "): "));

        territorio.alterarLimiar(codigo, tipo, nivel, valor);
        System.out.println("Limiar de " + nivel.getDescricao() + " para " + tipo.getDescricao()
                + " alterado para " + valor + " " + tipo.getUnidade() + ".");
    }

    private void alterarPopulacao() throws DadosInvalidosException {
        String texto = lerObrigatorio("Nova população estimada da região: ");
        try {
            territorio.setPopulacaoEstimada(Integer.parseInt(texto.replace(".", "")));
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("\"" + texto + "\" não é um número inteiro válido", e);
        }
        System.out.println("População estimada atualizada para " + territorio.getPopulacaoEstimada() + ".");
    }

    private void alterarEstrategiaPadrao() throws DadosInvalidosException {
        String codigo = lerObrigatorio("Código de acesso: ");
        listarOpcoes(TipoPrevisao.values(), TipoPrevisao::getDescricao);
        TipoPrevisao tipo = escolherPorNumero(lerObrigatorio("Escolha: "), TipoPrevisao.values());
        territorio.alterarEstrategiaPadrao(codigo, tipo);
        System.out.println("Estratégia padrão alterada para " + tipo.getDescricao() + ".");
    }

    // ---------- Opção 6 ----------

    /** Usa o Builder para montar o relatório parte por parte. */
    private void exibirStatusCompleto() {
        try {
            List<Sensor> sensores = todosOsSensores();
            Map<String, ResultadoPrevisao> previsoes = new LinkedHashMap<>();
            for (Sensor sensor : sensores) {
                previsoes.put(sensor.getNome(), servico.rodarPrevisao(sensor.getId()));
            }
            List<Alerta> alertas = servico.listarAlertasEmitidos();
            List<Alerta> recentes = alertas.subList(0, Math.min(QUANTIDADE_ALERTAS_NO_RELATORIO, alertas.size()));

            Relatorio relatorio = new RelatorioBuilder()
                    .comTerritorio(territorio)
                    .comStatusSensores(sensores)
                    .comUltimasPrevisoes(previsoes)
                    .comAlertasRecentes(recentes)
                    .build();
            System.out.println();
            System.out.println(relatorio.formatar());
        } catch (DadosInvalidosException e) {
            System.out.println("Não foi possível gerar o relatório: " + e.getMessage());
        }
    }

    // ---------- Opção 7 (bônus) ----------

    private void exportarHistoricoCsv() {
        try {
            Sensor sensor = buscarSensorPorId(lerObrigatorio("ID do sensor (" + listarIdsSensores() + "): "));
            List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getId());
            Path arquivo = exportadorCsv.exportar(sensor.getId(), historico);
            System.out.println(historico.size() + " medições exportadas para: " + arquivo.toAbsolutePath().normalize());
        } catch (DadosInvalidosException e) {
            System.out.println("Não foi possível exportar: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro ao gravar o arquivo CSV: " + e.getMessage()
                    + ". Verifique se a pasta permite escrita e se há espaço em disco.");
        }
    }

    // ---------- Apoio ----------

    private List<Sensor> todosOsSensores() {
        List<Sensor> sensores = new ArrayList<>();
        pontos.forEach(ponto -> sensores.addAll(ponto.getSensores()));
        return sensores;
    }

    private Sensor buscarSensorPorId(String sensorId) throws DadosInvalidosException {
        for (Sensor sensor : todosOsSensores()) {
            if (sensor.getId().equalsIgnoreCase(sensorId)) {
                return sensor;
            }
        }
        throw new DadosInvalidosException("Sensor \"" + sensorId + "\" não encontrado no território");
    }

    private String listarIdsSensores() {
        return String.join(", ", todosOsSensores().stream().map(Sensor::getId).toList());
    }

    /** Mostra as opções de um enum numeradas a partir de 1. */
    private static <E extends Enum<E>> void listarOpcoes(E[] opcoes, Function<E, String> descricao) {
        for (int i = 0; i < opcoes.length; i++) {
            System.out.println("  " + (i + 1) + ". " + descricao.apply(opcoes[i]));
        }
    }

    /** Converte o número digitado na opção correspondente (sem if por tipo). */
    private static <E extends Enum<E>> E escolherPorNumero(String texto, E[] opcoes) throws DadosInvalidosException {
        try {
            int indice = Integer.parseInt(texto.trim()) - 1;
            if (indice < 0 || indice >= opcoes.length) {
                throw new DadosInvalidosException("Opção " + texto + " não existe. Digite de 1 a " + opcoes.length);
            }
            return opcoes[indice];
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("\"" + texto + "\" não é um número de opção válido", e);
        }
    }

    /** Aceita vírgula ou ponto como separador decimal. */
    private static double converterDecimal(String texto) throws DadosInvalidosException {
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("\"" + texto + "\" não é um número válido", e);
        }
    }

    /** Lê uma resposta obrigatória; vazio ou fim de entrada viram exceção tratada pelo menu. */
    private String lerObrigatorio(String mensagem) throws DadosInvalidosException {
        String texto = lerObrigatorioOuVazio(mensagem);
        if (texto.isEmpty()) {
            throw new DadosInvalidosException("Nenhum valor informado");
        }
        return texto;
    }

    private String lerObrigatorioOuVazio(String mensagem) throws DadosInvalidosException {
        String texto = lerLinha(mensagem);
        if (texto == null) {
            throw new DadosInvalidosException("Entrada encerrada antes de concluir a operação");
        }
        return texto;
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
