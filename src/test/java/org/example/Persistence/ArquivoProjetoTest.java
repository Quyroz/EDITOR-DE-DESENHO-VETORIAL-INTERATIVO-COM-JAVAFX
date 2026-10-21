package org.example.Persistence;

import org.example.Models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ArquivoProjetoTest {
    @TempDir Path pasta;
    @Test void salvarEAbrirPreservaEstilosCamadasGruposEGeometria() throws Exception {
        DocumentoDesenho original = new DocumentoDesenho();
        Camada detalhes = new Camada("Detalhes");
        original.adicionarCamada(detalhes);
        Poligono p = new Poligono(List.of(new Ponto(0,0),new Ponto(20,0),new Ponto(0,30)));
        p.setLayerId(detalhes.getId());p.setGroupId("grupo-1");p.setCorPreenchimentoHex("#FF8000");
        original.adicionarForma(p);
        Circulo c = new Circulo(new Ponto(40,40),15);
        c.setLayerId(original.getIdCamadaInicial());c.cisalhar(.5,0);
        original.adicionarForma(c);
        Path caminho = pasta.resolve("projeto.json");
        ArquivoProjeto io = new ArquivoProjeto();
        io.salvar(caminho,original);
        DocumentoDesenho recuperado = io.carregar(caminho);
        assertEquals(2,recuperado.getFormas().size());
        assertEquals(2,recuperado.getCamadas().size());
        assertEquals("grupo-1",recuperado.buscarForma(p.getId()).getGroupId());
        assertEquals("#FF8000",recuperado.buscarForma(p.getId()).getCorPreenchimentoHex());
        assertEquals(c.calcularArea(),recuperado.buscarForma(c.getId()).calcularArea(),1e-8);
    }
    @Test void arquivoCorrompidoCausaErroControlado() throws Exception {
        Path caminho = pasta.resolve("invalido.json");
        Files.writeString(caminho,"{corrompido");
        assertThrows(java.io.IOException.class,()->new ArquivoProjeto().carregar(caminho));
    }
}
