package org.example.Controller.ferramentas;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Controller.EditorContext;
import org.example.Controller.Ferramenta;
import org.example.Models.*;

/** Arrastar um vértice converte formas regulares em polígonos livres. */
public final class FerramentaEditarVertice implements Ferramenta {
    private String formaId;
    private int indice = -1;
    private DocumentoDesenho anterior;
    private boolean moveu;
    @Override public String nome() { return "Editar vértices"; }
    @Override public void aoPressionarMouse(EditorContext editor, MouseEvent evento) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        formaId = null; indice = -1; moveu = false; anterior = null;
        java.util.List<FormaGeometrica> ordenadas = new java.util.ArrayList<>(editor.getDocumento().formasOrdenadas());
        java.util.Collections.reverse(ordenadas);
        for (FormaGeometrica f : ordenadas) {
            Camada c = editor.getDocumento().getCamada(f.getLayerId());
            if (!(f instanceof Poligono) || c == null || !c.isVisivel() || c.isBloqueada()) continue;
            for (int i = 0; i < f.getPontos().size(); i++) {
                if (f.getPontos().get(i).distancia(new Ponto(evento.getX(), evento.getY())) <= 9) {
                    formaId = f.getId(); indice = i; anterior = editor.getDocumento().clonar();
                    editor.selecionar(f, false); return;
                }
            }
        }
        editor.selecionar(editor.localizarForma(evento.getX(), evento.getY()), false);
    }
    @Override public void aoArrastarMouse(EditorContext editor, MouseEvent evento) {
        if (formaId == null || indice < 0) return;
        FormaGeometrica f = editor.getDocumento().buscarForma(formaId);
        if (!(f instanceof Poligono poligono)) return;
        Ponto p = editor.pontoMouse(evento.getX(), evento.getY());
        if (p.equals(poligono.getPontos().get(indice))) return;
        if (f instanceof Quadrado || f instanceof Hexagono) {
            Poligono livre = new Poligono(f.getId(), f.getPontos());
            livre.setCorPreenchimentoHex(f.getCorPreenchimentoHex());
            livre.setCorBordaHex(f.getCorBordaHex());
            livre.setEspessuraBorda(f.getEspessuraBorda());
            livre.setLayerId(f.getLayerId()); livre.setGroupId(f.getGroupId());
            editor.getDocumento().substituirForma(formaId, livre);
            poligono = livre;
        }
        poligono.alterarPonto(indice, p);
        moveu = true; editor.notificar();
    }
    @Override public void aoSoltarMouse(EditorContext editor, MouseEvent evento) {
        if (moveu && anterior != null) editor.registrarArrasto(anterior);
        formaId = null; indice = -1; anterior = null; moveu = false;
    }
    @Override public void aoDesativar(EditorContext editor) { aoSoltarMouse(editor, null); }
}
