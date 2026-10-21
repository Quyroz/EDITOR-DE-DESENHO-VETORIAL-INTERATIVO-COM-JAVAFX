package org.example.Controller.ferramentas;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Controller.EditorContext;
import org.example.Controller.Ferramenta;
import org.example.Models.*;

/** Desenho por arrasto de formas regulares. */
public final class FerramentaInserirForma implements Ferramenta {
    public enum Tipo { CIRCULO, QUADRADO, HEXAGONO }
    private final Tipo tipo;
    private Ponto inicio;
    private Ponto fim;

    public FerramentaInserirForma(Tipo tipo) { this.tipo = tipo; }
    @Override public String nome() { return switch (tipo) {
        case CIRCULO -> "Círculo"; case QUADRADO -> "Quadrado"; case HEXAGONO -> "Hexágono"; }; }
    public Tipo getTipo() { return tipo; }
    public Ponto getInicio() { return inicio; }
    public Ponto getFim() { return fim; }
    @Override public void aoPressionarMouse(EditorContext editor, MouseEvent evento) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        if (!editor.camadaEditavel()) { editor.setMensagem("Camada bloqueada ou oculta."); return; }
        inicio = editor.pontoMouse(evento.getX(), evento.getY());
        fim = inicio;
        editor.notificar();
    }
    @Override public void aoArrastarMouse(EditorContext editor, MouseEvent evento) {
        if (inicio == null) return;
        fim = editor.pontoMouse(evento.getX(), evento.getY());
        editor.notificar();
    }
    @Override public void aoSoltarMouse(EditorContext editor, MouseEvent evento) {
        if (inicio == null) return;
        fim = editor.pontoMouse(evento.getX(), evento.getY());
        final Ponto a = inicio, b = fim;
        inicio = null; fim = null;
        double dx = b.getX()-a.getX(), dy = b.getY()-a.getY();
        double distancia = Math.hypot(dx,dy);
        if (distancia < 2) { editor.setMensagem("Arraste para definir o tamanho da forma."); return; }
        editor.executar(d -> {
            FormaGeometrica forma = switch (tipo) {
                case CIRCULO -> new Circulo(a, distancia);
                case HEXAGONO -> new Hexagono(a, distancia);
                case QUADRADO -> {
                    double lado = Math.max(Math.abs(dx), Math.abs(dy));
                    Ponto origem = new Ponto(dx < 0 ? a.getX()-lado : a.getX(),
                                           dy < 0 ? a.getY()-lado : a.getY());
                    yield new Quadrado(origem, lado);
                }
            };
            editor.aplicarEstilo(forma);
            d.adicionarForma(forma);
        });
    }
    @Override public void aoDesativar(EditorContext editor) { inicio = fim = null; editor.notificar(); }
}
