package org.example.Models;

import java.util.List;

/** Transformações conjuntas em relação ao pivô compartilhado (Composite lógico). */
public final class Transformacoes {
    private Transformacoes() { }
    public static Ponto centroConjunto(List<FormaGeometrica> formas) {
        if (formas.isEmpty()) throw new IllegalArgumentException("Seleção vazia.");
        double x = 0, y = 0;
        for (FormaGeometrica f : formas) { x += f.getCentroide().getX(); y += f.getCentroide().getY(); }
        return new Ponto(x/formas.size(), y/formas.size());
    }
    public static void transladar(List<FormaGeometrica> formas, double dx, double dy) {
        for (FormaGeometrica f : formas) f.transladar(dx, dy);
    }
    public static void escalar(List<FormaGeometrica> formas, double fator) {
        Ponto.validarNumero(fator, "Fator");
        if (fator <= 0) throw new IllegalArgumentException("Escala deve ser positiva.");
        if (formas.isEmpty()) return;
        Ponto c = centroConjunto(formas);
        for (FormaGeometrica f : formas) {
            Ponto a = f.getCentroide();
            f.escalar(fator);
            f.transladar((fator-1)*(a.getX()-c.getX()), (fator-1)*(a.getY()-c.getY()));
        }
    }
    public static void rotacionar(List<FormaGeometrica> formas, double angulo) {
        Ponto.validarNumero(angulo, "Ângulo");
        if (formas.isEmpty()) return;
        Ponto c = centroConjunto(formas);
        double cos = Math.cos(angulo), sin = Math.sin(angulo);
        for (FormaGeometrica f : formas) {
            Ponto a = f.getCentroide();
            double dx = a.getX()-c.getX(), dy = a.getY()-c.getY();
            f.rotacionar(angulo);
            f.transladar((cos-1)*dx-sin*dy, sin*dx+(cos-1)*dy);
        }
    }
    public static void cisalhar(List<FormaGeometrica> formas, double shx, double shy) {
        if (formas.isEmpty()) return;
        Ponto c = centroConjunto(formas);
        for (FormaGeometrica f : formas) {
            Ponto a = f.getCentroide();
            f.cisalhar(shx, shy);
            f.transladar(shx*(a.getY()-c.getY()), shy*(a.getX()-c.getX()));
        }
    }
}
