package org.example.Models;

import java.util.ArrayList;
import java.util.List;

/** Hexágono regular definido por centro e raio circunscrito. */
public final class Hexagono extends Poligono {
    public Hexagono(Ponto centro, double raio) { this(null, vertices(centro, raio)); }
    public Hexagono(String id, List<Ponto> vertices) {
        super(id, vertices);
        if (vertices.size() != 6) throw new IllegalArgumentException("Hexágono deve ter seis vértices.");
    }
    private static List<Ponto> vertices(Ponto centro, double raio) {
        Ponto.validarNumero(raio, "Raio");
        if (raio <= 0) throw new IllegalArgumentException("O raio do hexágono deve ser positivo.");
        List<Ponto> pontos = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            double angulo = (i * Math.PI / 3.0);
            pontos.add(centro.somar(raio * Math.cos(angulo), raio * Math.sin(angulo)));
        }
        return pontos;
    }
    @Override public void adicionarPonto(Ponto ponto) {
        throw new UnsupportedOperationException("Formas regulares não aceitam vértices adicionais.");
    }
    @Override public void alterarPonto(int indice, Ponto ponto) {
        throw new UnsupportedOperationException("Converta a forma regular em polígono livre antes de editar vértices.");
    }
    @Override public Hexagono clonar() {
        Hexagono copia = new Hexagono(getId(), getPontos());
        copiarAtributosPara(copia);
        return copia;
    }
}
