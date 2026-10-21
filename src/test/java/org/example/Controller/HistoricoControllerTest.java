package org.example.Controller;

import org.example.Models.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HistoricoControllerTest {
    @Test void desfazerERefazerDeCriacao() {
        HistoricoController historico = new HistoricoController();
        DocumentoDesenho d = new DocumentoDesenho();
        historico.executar(doc -> {
            Circulo c = new Circulo(new Ponto(20,20),10);
            c.setLayerId(doc.getIdCamadaInicial());
            doc.adicionarForma(c);
        },d);
        assertEquals(1,d.getFormas().size());
        d = historico.desfazer(d);
        assertEquals(0,d.getFormas().size());
        d = historico.refazer(d);
        assertEquals(1,d.getFormas().size());
    }
    @Test void novaAcaoDescartaPilhaDeRefazer() {
        HistoricoController h = new HistoricoController();
        DocumentoDesenho d = new DocumentoDesenho();
        h.executar(doc->doc.setMostrarGrade(false),d);
        d = h.desfazer(d);
        assertTrue(h.podeRefazer());
        h.executar(doc->doc.setSnapAtivo(false),d);
        assertFalse(h.podeRefazer());
    }
}
