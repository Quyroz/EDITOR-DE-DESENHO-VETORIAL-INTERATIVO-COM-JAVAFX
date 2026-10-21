package org.example.Controller.ferramentas;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Controller.EditorContext;
import org.example.Controller.Ferramenta;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import java.util.ArrayList;
import java.util.List;

/** Cliques acrescentam vértices. Clique duplo conclui, botão direito cancela. */
public final class FerramentaDesenhar implements Ferramenta {
    private final List<Ponto> vertices = new ArrayList<>();
    private Ponto cursor;
    @Override public String nome() { return "Polígono livre"; }
    public List<Ponto> getVertices() { return List.copyOf(vertices); }
    public Ponto getCursor() { return cursor; }
    public void atualizarCursor(EditorContext editor, double x, double y) {
        cursor = editor.pontoMouse(x,y);
        editor.notificar();
    }
    @Override public void aoClicar(EditorContext editor, MouseEvent evento) {
        if (evento.getButton() == MouseButton.SECONDARY) { limpar(editor); return; }
        if (evento.getButton() != MouseButton.PRIMARY) return;
        if (!editor.camadaEditavel()) { editor.setMensagem("Ative uma camada visível e desbloqueada."); return; }
        if (evento.getClickCount() >= 2) {
            if (vertices.size() >= 3) {
                List<Ponto> pontos = List.copyOf(vertices);
                editor.executar(d -> { Poligono p = new Poligono(pontos); editor.aplicarEstilo(p); d.adicionarForma(p); });
                limpar(editor);
            } else editor.setMensagem("O polígono precisa de pelo menos 3 vértices.");
            return;
        }
        Ponto p = editor.pontoMouse(evento.getX(), evento.getY());
        if (vertices.isEmpty() || !vertices.get(vertices.size()-1).equals(p)) vertices.add(p);
        editor.setMensagem(vertices.size() + " vértice(s) · duplo clique para concluir · direito para cancelar");
    }
    public void limpar(EditorContext editor) { vertices.clear(); cursor = null; editor.notificar(); }
    @Override public void aoDesativar(EditorContext editor) { limpar(editor); }
}
