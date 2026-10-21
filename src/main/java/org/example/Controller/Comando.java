package org.example.Controller;

import org.example.Models.DocumentoDesenho;

/** Command: a ação é encapsulada e registrada pelo gerenciador de histórico. */
@FunctionalInterface
public interface Comando {
    void executar(DocumentoDesenho documento);
}
