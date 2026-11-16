package br.com.fiap.tpi.excecao;

/**
 * Lançada quando um dado do território é inválido: medição fora da faixa,
 * população negativa, limiar incoerente ou código de acesso errado.
 * É checada (extends Exception) para obrigar quem chama a tratar o problema.
 */
public class DadosInvalidosException extends Exception {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
