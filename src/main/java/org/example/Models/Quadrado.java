package org.example.Models;

import java.util.List;

/** Quadrado regular gerado automaticamente a partir de uma origem e do lado. */
public final class Quadrado extends Poligono {
    public Quadrado(Ponto origem, double lado) { this(null, vertices(origem, lado)); }
    public Quadrado(String id, List<Ponto> vertices) {
        super(id, vertices);
        if (vertices.size() != 4) throw new IllegalArgumentException("Quadrado deve ter quatro vértices.");
    }
    private static List<Ponto> vertices(Ponto p, double lado) {
        Ponto.validarNumero(lado, "Lado");
        if (lado <= 0) throw new IllegalArgumentException("O lado do quadrado deve ser positivo.");
        return List.of(p, p.somar(lado,0), p.somar(lado,lado), p.somar(0,lado));
    }
    @Override public void adicionarPonto(Ponto ponto) {
        throw new UnsupportedOperationException("Formas regulares não aceitam vértices adicionais.");
    }
    @Override public void alterarPonto(int indice, Ponto ponto) {
        throw new UnsupportedOperationException("Converta a forma regular em polígono livre antes de editar vértices.");
    }
    @Override public Quadrado clonar() {
        Quadrado copia = new Quadrado(getId(), getPontos());
        copiarAtributosPara(copia);
        return copia;
    }
}
