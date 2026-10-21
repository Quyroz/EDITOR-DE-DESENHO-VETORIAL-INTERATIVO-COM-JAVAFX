package org.example.Controller.ferramentas;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Controller.EditorContext;
import org.example.Controller.Ferramenta;
import org.example.Models.*;

/** Arraste para transladar uma seleção/grupo. Uma única entrada no Undo ao soltar. */
public final class FerramentaTransformar implements Ferramenta {
    private Ponto ultimo;
    private DocumentoDesenho antes;
    private boolean moveu;
    @Override public String nome() { return "Mover / transformar"; }
    @Override public void aoPressionarMouse(EditorContext editor, MouseEvent evento) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        FormaGeometrica f = editor.localizarForma(evento.getX(), evento.getY());
        if (f == null) { editor.limparSelecao(); return; }
        if (!editor.getSelecionados().contains(f.getId())) editor.selecionar(f, false);
        ultimo = editor.pontoMouse(evento.getX(), evento.getY());
        antes = editor.getDocumento().clonar();
        moveu = false;
    }
    @Override public void aoArrastarMouse(EditorContext editor, MouseEvent evento) {
        if (ultimo == null) return;
        Ponto proximo = editor.pontoMouse(evento.getX(), evento.getY());
        double dx = proximo.getX()-ultimo.getX(), dy = proximo.getY()-ultimo.getY();
        if (dx != 0 || dy != 0) {
            Transformacoes.transladar(editor.formasSelecionadas(), dx, dy);
            ultimo = proximo;
            moveu = true;
            editor.notificar();
        }
    }
    @Override public void aoSoltarMouse(EditorContext editor, MouseEvent evento) {
        if (moveu && antes != null) editor.registrarArrasto(antes);
        ultimo = null; antes = null; moveu = false;
    }
    @Override public void aoDesativar(EditorContext editor) { aoSoltarMouse(editor, null); }
}
