package org.example.Models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GeometriaTest {
    private static Poligono triangulo() {
        return new Poligono(List.of(new Ponto(0,0),new Ponto(4,0),new Ponto(0,3)));
    }

    @Test void areaPerimetroECentroideDoTriangulo() {
        Poligono p = triangulo();
        assertEquals(6,p.calcularArea(),1e-9);
        assertEquals(12,p.calcularPerimetro(),1e-9);
        assertEquals(4.0/3,p.getCentroide().getX(),1e-9);
        assertEquals(1.0,p.getCentroide().getY(),1e-9);
    }
    @Test void testeDeContencaoPorRayCasting() {
        assertTrue(triangulo().contemPonto(1,1));
        assertFalse(triangulo().contemPonto(20,20));
    }
    @Test void poligonoInvalidoRejeitado() {
        assertThrows(IllegalArgumentException.class,()->new Poligono(List.of(new Ponto(1,1),new Ponto(2,2))));
    }
    @Test void circunferenciaEArea() {
        Circulo c = new Circulo(new Ponto(0,0),10);
        assertEquals(100*Math.PI,c.calcularArea(),1e-8);
        assertEquals(20*Math.PI,c.calcularPerimetro(),1e-5);
        assertTrue(c.contemPonto(2,3));
        assertFalse(c.contemPonto(11,0));
    }
    @Test void circuloComRaioZeroInvalido() {
        assertThrows(IllegalArgumentException.class,()->new Circulo(new Ponto(0,0),0));
    }
    @Test void escalaRotacaoECisalhamentoDePoligono() {
        Poligono p = triangulo();
        p.escalar(2);
        assertEquals(24,p.calcularArea(),1e-8);
        p.rotacionar(Math.PI/2);
        assertEquals(24,p.calcularArea(),1e-8);
        p.cisalhar(.5,0);
        assertEquals(24,p.calcularArea(),1e-8);
    }
    @Test void cisalhamentoDoCirculoPreservaAreaQuandoDeterminanteUm() {
        Circulo c = new Circulo(new Ponto(100,100),15);
        c.cisalhar(.7,0);
        assertEquals(225*Math.PI,c.calcularArea(),1e-6);
        assertTrue(c.contemPonto(100,100));
    }
    @Test void quadradoEHexagonoRegulares() {
        Quadrado q = new Quadrado(new Ponto(0,0),4);
        Hexagono h = new Hexagono(new Ponto(0,0),2);
        assertEquals(16,q.calcularArea(),1e-9);
        assertEquals(16,q.calcularPerimetro(),1e-9);
        assertEquals(6,h.getQuantidadePontos());
        assertEquals(6*Math.sqrt(3),h.calcularArea(),1e-8);
    }
    @Test void clonarPreservaIdentidadeMasIsolaVertices() {
        Poligono original = triangulo();
        Poligono copia = original.clonar();
        copia.transladar(10,10);
        assertEquals(original.getId(),copia.getId());
        assertNotEquals(original.getPontos().get(0),copia.getPontos().get(0));
        assertEquals(0,original.getPontos().get(0).getX(),1e-9);
    }
    @Test void agrupamentoEscalaEmTornoDePivoGlobal() {
        Circulo a = new Circulo(new Ponto(0,0),10);
        Circulo b = new Circulo(new Ponto(100,0),10);
        Transformacoes.escalar(List.of(a,b),2);
        assertEquals(-50,a.getCentro().getX(),1e-9);
        assertEquals(150,b.getCentro().getX(),1e-9);
        assertEquals(20,a.getRaio(),1e-9);
    }
    @Test void snapshotCopiaProfundamenteCamadasEFormas() {
        DocumentoDesenho d = new DocumentoDesenho();
        Poligono p = triangulo(); p.setLayerId(d.getIdCamadaInicial());d.adicionarForma(p);
        DocumentoDesenho copia = d.clonar();
        ((Poligono)copia.buscarForma(p.getId())).transladar(10,0);
        copia.getCamadas().get(0).setNome("Alterada");
        assertEquals(0.0,d.buscarForma(p.getId()).getPontos().get(0).getX(),1e-9);
        assertEquals("Camada 1",d.getCamadas().get(0).getNome());
    }
    @Test void snapAproximaPontos() {
        DocumentoDesenho d = new DocumentoDesenho();
        assertEquals(new Ponto(20,40),d.aplicarSnap(new Ponto(27,32)));
        d.setSnapAtivo(false);
        assertEquals(new Ponto(27,32),d.aplicarSnap(new Ponto(27,32)));
    }
}
