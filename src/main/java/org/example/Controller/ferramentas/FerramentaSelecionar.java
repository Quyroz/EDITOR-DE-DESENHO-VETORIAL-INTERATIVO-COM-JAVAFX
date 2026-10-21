package org.example.Controller.ferramentas;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Controller.EditorContext;
import org.example.Controller.Ferramenta;

public final class FerramentaSelecionar implements Ferramenta {
    @Override public String nome() { return "Selecionar"; }
    @Override public void aoClicar(EditorContext editor, MouseEvent evento) {
        if (evento.getButton() == MouseButton.PRIMARY)
            editor.selecionar(editor.localizarForma(evento.getX(), evento.getY()), evento.isControlDown() || evento.isShiftDown());
    }
}
