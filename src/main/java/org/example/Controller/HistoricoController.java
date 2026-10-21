package org.example.Controller;

import org.example.Models.DocumentoDesenho;
import java.util.ArrayDeque;
import java.util.Deque;

/** Undo/Redo via snapshots (Memento). Limite de memória por número de ações. */
public final class HistoricoController {
    private static final int MAX_ESTADOS = 100;
    private final Deque<EditorMemento> desfazer = new ArrayDeque<>();
    private final Deque<EditorMemento> refazer = new ArrayDeque<>();

    public void executar(Comando comando, DocumentoDesenho documento) {
        EditorMemento anterior = new EditorMemento(documento);
        try {
            comando.executar(documento);
            documento.validarIntegridade();
        } catch (RuntimeException erro) {
            // Uma operação com falha não entra no histórico; caller pode restaurar anterior.
            throw erro;
        }
        registrarMemento(anterior);
    }
    /** Utilizado uma única vez ao concluir arrastos (transações interativas). */
    public void registrarEstado(DocumentoDesenho estadoAnterior) { registrarMemento(new EditorMemento(estadoAnterior)); }
    private void registrarMemento(EditorMemento anterior) {
        desfazer.push(anterior);
        while (desfazer.size() > MAX_ESTADOS) desfazer.removeLast();
        refazer.clear();
    }
    public DocumentoDesenho desfazer(DocumentoDesenho atual) {
        if (desfazer.isEmpty()) return atual;
        refazer.push(new EditorMemento(atual));
        return desfazer.pop().restaurar();
    }
    public DocumentoDesenho refazer(DocumentoDesenho atual) {
        if (refazer.isEmpty()) return atual;
        desfazer.push(new EditorMemento(atual));
        return refazer.pop().restaurar();
    }
    public boolean podeDesfazer() { return !desfazer.isEmpty(); }
    public boolean podeRefazer() { return !refazer.isEmpty(); }
    public void limpar() { desfazer.clear(); refazer.clear(); }
}
