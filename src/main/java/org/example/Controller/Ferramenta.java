package org.example.Controller;

import javafx.scene.input.MouseEvent;

/** State: ações da interface delegadas à ferramenta atual. */
public interface Ferramenta {
    String nome();
    default void aoPressionarMouse(EditorContext editor, MouseEvent evento) { }
    default void aoArrastarMouse(EditorContext editor, MouseEvent evento) { }
    default void aoSoltarMouse(EditorContext editor, MouseEvent evento) { }
    default void aoClicar(EditorContext editor, MouseEvent evento) { }
    default void aoDesativar(EditorContext editor) { }
}
