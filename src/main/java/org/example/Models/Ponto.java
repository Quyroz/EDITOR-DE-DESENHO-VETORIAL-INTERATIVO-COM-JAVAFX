package org.example.Models;

/** Um ponto cartesiano finito. Imutável para evitar alteração externa de vértices. */
public final class Ponto {
    private final double x;
    private final double y;

    public Ponto(double x, double y) {
        validarNumero(x, "x");
        validarNumero(y, "y");
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public Ponto somar(double dx, double dy) { return new Ponto(x + dx, y + dy); }
    public double distancia(Ponto outro) { return Math.hypot(x - outro.x, y - outro.y); }
    public static void validarNumero(double numero, String atributo) {
        if (!Double.isFinite(numero)) throw new IllegalArgumentException(atributo + " deve ser finito.");
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof Ponto p
                && Double.compare(x, p.x) == 0 && Double.compare(y, p.y) == 0;
    }
    @Override public int hashCode() { return java.util.Objects.hash(x, y); }
    @Override public String toString() { return "(" + x + ", " + y + ")"; }
}
