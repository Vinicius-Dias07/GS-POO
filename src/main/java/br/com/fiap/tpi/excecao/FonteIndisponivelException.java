package br.com.fiap.tpi.excecao;

/**
 * Lançada quando uma fonte de dados (sensor ou gerador simulado) não responde.
 */
public class FonteIndisponivelException extends Exception {

    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public FonteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
