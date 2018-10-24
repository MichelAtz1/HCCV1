package br.desenvolvedor.michelatz.aplicativohcc.Modelo;

public class DadosListagem {

    private String id;
    private String nota;
    private String nome;
    private String data;

    public DadosListagem() {

    }

    public DadosListagem(String id, String nota, String nome, String data) {
        this.id = id;
        this.nota = nota;
        this.nome = nome;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
