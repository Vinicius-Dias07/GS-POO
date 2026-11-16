package br.com.fiap.tpi.domain;

import br.com.fiap.tpi.exception.DadosInvalidosException;
import br.com.fiap.tpi.service.ConfiguracaoSistema;

/**
 * Dados gerais do território monitorado. Descreve a REGIÃO, nunca pessoas.
 * Alterações sensíveis (limiares e estratégia) exigem o código de acesso,
 * que é privado e não tem getter — só pode ser conferido, nunca lido.
 */
public class DadosTerritorio {

    private final String nomeRegiao;
    private int populacaoEstimada;
    private int quantidadePontosVulneraveis;
    private final String codigoAcesso;
    private final ConfiguracaoSistema configuracao;

    public DadosTerritorio(String nomeRegiao, int populacaoEstimada, int quantidadePontosVulneraveis,
                           String codigoAcesso, ConfiguracaoSistema configuracao) throws DadosInvalidosException {
        if (nomeRegiao == null || nomeRegiao.isBlank()) {
            throw new DadosInvalidosException("O nome da região não pode ser vazio");
        }
        if (codigoAcesso == null || codigoAcesso.isBlank()) {
            throw new DadosInvalidosException("O código de acesso do território não pode ser vazio");
        }
        if (configuracao == null) {
            throw new DadosInvalidosException("O território precisa de uma configuração do sistema");
        }
        this.nomeRegiao = nomeRegiao.trim();
        this.codigoAcesso = codigoAcesso;
        this.configuracao = configuracao;
        setPopulacaoEstimada(populacaoEstimada);
        setQuantidadePontosVulneraveis(quantidadePontosVulneraveis);
    }

    /**
     * Altera um limiar de alerta. Confere o código e delega a validação
     * de faixa/coerência para a ConfiguracaoSistema.
     */
    public void alterarLimiar(String codigo, TipoSensor tipo, NivelAlerta nivel, double valor)
            throws DadosInvalidosException {
        conferirCodigo(codigo);
        if (valor < 0) {
            throw new DadosInvalidosException("O limiar não pode ser negativo: " + valor);
        }
        configuracao.definirLimiar(tipo, nivel, valor);
    }

    public void alterarEstrategiaPadrao(String codigo, TipoPrevisao tipo) throws DadosInvalidosException {
        conferirCodigo(codigo);
        configuracao.setEstrategiaPadrao(tipo);
    }

    private void conferirCodigo(String codigo) throws DadosInvalidosException {
        if (!codigoAcesso.equals(codigo)) {
            throw new DadosInvalidosException("Código de acesso inválido");
        }
    }

    public String getNomeRegiao() {
        return nomeRegiao;
    }

    public int getPopulacaoEstimada() {
        return populacaoEstimada;
    }

    public void setPopulacaoEstimada(int populacaoEstimada) throws DadosInvalidosException {
        if (populacaoEstimada < 0) {
            throw new DadosInvalidosException("A população estimada não pode ser negativa: " + populacaoEstimada);
        }
        this.populacaoEstimada = populacaoEstimada;
    }

    public int getQuantidadePontosVulneraveis() {
        return quantidadePontosVulneraveis;
    }

    public void setQuantidadePontosVulneraveis(int quantidade) throws DadosInvalidosException {
        if (quantidade < 0) {
            throw new DadosInvalidosException("A quantidade de pontos vulneráveis não pode ser negativa: " + quantidade);
        }
        this.quantidadePontosVulneraveis = quantidade;
    }
}
