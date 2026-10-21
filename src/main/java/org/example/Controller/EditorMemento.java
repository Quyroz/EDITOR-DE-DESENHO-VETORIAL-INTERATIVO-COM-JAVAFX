package org.example.Controller;

import org.example.Models.DocumentoDesenho;

/** Memento não expõe seu estado mutável: cada restauração retorna clone profundo. */
public final class EditorMemento {
    private final DocumentoDesenho estado;
    public EditorMemento(DocumentoDesenho documento) { estado = documento.clonar(); }
    public DocumentoDesenho restaurar() { return estado.clonar(); }
}
