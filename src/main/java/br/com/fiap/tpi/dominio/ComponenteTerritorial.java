package br.com.fiap.tpi.dominio;

import br.com.fiap.tpi.excecao.DadosInvalidosException;

/**
 * Base abstrata de tudo que existe no território (pontos vulneráveis e sensores).
 * Encapsula os dados comuns com atributos privados e obriga cada filha a
 * dizer como verifica o próprio status (método abstrato → polimorfismo).
 */
public abstract class ComponenteTerritorial {

    private final String id;
    private String nome;
    private String regiao;
    private StatusComponente status;

    protected ComponenteTerritorial(String id, String nome, String regiao) throws DadosInvalidosException {
        if (textoVazio(id)) {
            throw new DadosInvalidosException("O identificador do componente não pode ser vazio");
        }
        this.id = id;
        setNome(nome);
        setRegiao(regiao);
        this.status = StatusComponente.OPERANTE;
    }

    /** Cada tipo de componente descreve sua situação de um jeito próprio. */
    public abstract String verificarStatus();

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) throws DadosInvalidosException {
        if (textoVazio(nome)) {
            throw new DadosInvalidosException("O nome do componente " + id + " não pode ser vazio");
        }
        this.nome = nome.trim();
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (textoVazio(regiao)) {
            throw new DadosInvalidosException("A região do componente " + id + " não pode ser vazia");
        }
        this.regiao = regiao.trim();
    }

    public StatusComponente getStatus() {
        return status;
    }

    public void setStatus(StatusComponente status) throws DadosInvalidosException {
        if (status == null) {
            throw new DadosInvalidosException("O status do componente " + id + " não pode ser nulo");
        }
        this.status = status;
    }

    private static boolean textoVazio(String texto) {
        return texto == null || texto.isBlank();
    }
}
