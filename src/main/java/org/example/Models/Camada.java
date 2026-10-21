package org.example.Models;

import java.util.UUID;

public final class Camada {
    private final String id;
    private String nome;
    private boolean visivel;
    private boolean bloqueada;

    public Camada(String nome) { this(UUID.randomUUID().toString(), nome, true, false); }
    public Camada(String id, String nome, boolean visivel, boolean bloqueada) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID da camada vazio.");
        this.id = id;
        setNome(nome);
        this.visivel = visivel;
        this.bloqueada = bloqueada;
    }
    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome da camada vazio.");
        this.nome = nome.trim();
    }
    public boolean isVisivel() { return visivel; }
    public void setVisivel(boolean visivel) { this.visivel = visivel; }
    public boolean isBloqueada() { return bloqueada; }
    public void setBloqueada(boolean bloqueada) { this.bloqueada = bloqueada; }
    public Camada clonar() { return new Camada(id, nome, visivel, bloqueada); }
    @Override public String toString() { return nome; }
}
