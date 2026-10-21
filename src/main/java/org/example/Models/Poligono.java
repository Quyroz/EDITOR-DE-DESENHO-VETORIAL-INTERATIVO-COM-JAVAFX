package org.example.Models;

import java.util.ArrayList;
import java.util.List;

/** Polígono fechado; usa fórmula do cadarço (Gauss) e teste ray casting. */
public class Poligono extends FormaBase {
    private final List<Ponto> pontos;

    public Poligono(List<Ponto> pontos) { this(null, pontos); }
    public Poligono(String id, List<Ponto> pontos) {
        super(id == null ? java.util.UUID.randomUUID().toString() : id);
        if (pontos == null || pontos.size() < 3) throw new IllegalArgumentException("O polígono exige no mínimo 3 pontos.");
        this.pontos = new ArrayList<>();
        for (Ponto p : pontos) {
            if (p == null) throw new IllegalArgumentException("Ponto nulo.");
            this.pontos.add(p);
        }
    }
    @Override public void adicionarPonto(Ponto ponto) {
        if (ponto == null) throw new IllegalArgumentException("Ponto nulo.");
        pontos.add(ponto);
    }
    public void alterarPonto(int indice, Ponto ponto) {
        if (ponto == null) throw new IllegalArgumentException("Ponto nulo.");
        pontos.set(indice, ponto);
    }
    @Override public List<Ponto> getPontos() { return List.copyOf(pontos); }
    @Override public int getQuantidadePontos() { return pontos.size(); }

    /** A área é sempre não negativa, independente da orientação dos vértices. */
    @Override public double calcularArea() {
        double soma = 0;
        for (int i = 0; i < pontos.size(); i++) {
            Ponto a = pontos.get(i), b = pontos.get((i + 1) % pontos.size());
            soma += a.getX() * b.getY() - b.getX() * a.getY();
        }
        return Math.abs(soma) / 2.0;
    }
    @Override public double calcularPerimetro() {
        double soma = 0;
        for (int i = 0; i < pontos.size(); i++) soma += pontos.get(i).distancia(pontos.get((i + 1) % pontos.size()));
        return soma;
    }
    @Override public Ponto getCentroide() {
        double areaDupla = 0, somaX = 0, somaY = 0;
        for (int i = 0; i < pontos.size(); i++) {
            Ponto a = pontos.get(i), b = pontos.get((i + 1) % pontos.size());
            double termo = a.getX() * b.getY() - b.getX() * a.getY();
            areaDupla += termo;
            somaX += (a.getX() + b.getX()) * termo;
            somaY += (a.getY() + b.getY()) * termo;
        }
        if (Math.abs(areaDupla) < 1e-10) {
            double x = 0, y = 0;
            for (Ponto p : pontos) { x += p.getX(); y += p.getY(); }
            return new Ponto(x / pontos.size(), y / pontos.size());
        }
        return new Ponto(somaX / (3 * areaDupla), somaY / (3 * areaDupla));
    }
    @Override public boolean contemPonto(double x, double y) {
        boolean dentro = false;
        for (int i = 0, j = pontos.size() - 1; i < pontos.size(); j = i++) {
            Ponto a = pontos.get(i), b = pontos.get(j);
            if (distanciaSegmento(x, y, a, b) <= Math.max(4, getEspessuraBorda() / 2 + 2)) return true;
            if ((a.getY() > y) != (b.getY() > y)
                    && x < (b.getX() - a.getX()) * (y - a.getY()) / (b.getY() - a.getY()) + a.getX()) dentro = !dentro;
        }
        return dentro;
    }
    private static double distanciaSegmento(double x, double y, Ponto a, Ponto b) {
        double dx = b.getX() - a.getX(), dy = b.getY() - a.getY();
        double comprimento2 = dx * dx + dy * dy;
        if (comprimento2 < 1e-12) return Math.hypot(x - a.getX(), y - a.getY());
        double t = Math.max(0, Math.min(1, ((x-a.getX())*dx + (y-a.getY())*dy) / comprimento2));
        return Math.hypot(x - a.getX() - t*dx, y - a.getY() - t*dy);
    }
    @Override public void transladar(double dx, double dy) { transformar(p -> p.somar(dx, dy)); }
    @Override public void escalar(double fator) {
        validarFator(fator);
        Ponto c = getCentroide();
        transformar(p -> new Ponto(c.getX() + fator*(p.getX()-c.getX()), c.getY()+fator*(p.getY()-c.getY())));
    }
    @Override public void rotacionar(double anguloRadianos) {
        Ponto.validarNumero(anguloRadianos, "Ângulo");
        Ponto c = getCentroide();
        double co = Math.cos(anguloRadianos), si = Math.sin(anguloRadianos);
        transformar(p -> { double x = p.getX()-c.getX(), y = p.getY()-c.getY();
            return new Ponto(c.getX()+co*x-si*y, c.getY()+si*x+co*y); });
    }
    @Override public void cisalhar(double shx, double shy) {
        Ponto.validarNumero(shx, "shx"); Ponto.validarNumero(shy, "shy");
        Ponto c = getCentroide();
        transformar(p -> { double x = p.getX()-c.getX(), y = p.getY()-c.getY();
            return new Ponto(c.getX()+x+shx*y, c.getY()+y+shy*x); });
    }
    protected static void validarFator(double fator) {
        Ponto.validarNumero(fator, "Fator");
        if (fator <= 0) throw new IllegalArgumentException("O fator de escala deve ser positivo.");
    }
    private void transformar(java.util.function.UnaryOperator<Ponto> operacao) {
        for (int i = 0; i < pontos.size(); i++) pontos.set(i, operacao.apply(pontos.get(i)));
    }
    @Override public Poligono clonar() {
        Poligono copia = new Poligono(getId(), pontos);
        copiarAtributosPara(copia);
        return copia;
    }
}
