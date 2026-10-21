package org.example.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Círculo com dois vetores-base: após cisalhamento torna-se uma elipse afim.
 * Assim a transformação não é perdida e área/hit-test permanecem corretos.
 */
public final class Circulo extends FormaBase {
    private Ponto centro;
    private Ponto eixoU;
    private Ponto eixoV;

    public Circulo(Ponto centro, double raio) {
        if (centro == null) throw new IllegalArgumentException("Centro obrigatório.");
        Ponto.validarNumero(raio, "Raio");
        if (raio <= 0) throw new IllegalArgumentException("O raio deve ser estritamente positivo.");
        this.centro = centro;
        eixoU = new Ponto(raio, 0);
        eixoV = new Ponto(0, raio);
    }
    public Circulo(String id, Ponto centro, Ponto eixoU, Ponto eixoV) {
        super(id);
        if (centro == null || eixoU == null || eixoV == null)
            throw new IllegalArgumentException("Parâmetros do círculo incompletos.");
        if (Math.abs(eixoU.getX()*eixoV.getY() - eixoU.getY()*eixoV.getX()) < 1e-9)
            throw new IllegalArgumentException("Os eixos da elipse não podem ser degenerados.");
        this.centro = centro; this.eixoU = eixoU; this.eixoV = eixoV;
    }
    public Ponto getCentro() { return centro; }
    public Ponto getEixoU() { return eixoU; }
    public Ponto getEixoV() { return eixoV; }
    public double getRaio() { return Math.hypot(eixoU.getX(), eixoU.getY()); }
    @Override public void adicionarPonto(Ponto ponto) {
        throw new UnsupportedOperationException("Círculos são definidos por centro e eixos, não por vértices.");
    }
    @Override public List<Ponto> getPontos() { return List.of(centro); }
    @Override public int getQuantidadePontos() { return 1; }
    public Ponto pontoContorno(double angulo) {
        return centro.somar(eixoU.getX()*Math.cos(angulo) + eixoV.getX()*Math.sin(angulo),
                           eixoU.getY()*Math.cos(angulo) + eixoV.getY()*Math.sin(angulo));
    }
    public List<Ponto> amostrarContorno(int amostras) {
        if (amostras < 3) throw new IllegalArgumentException("Amostragem inválida.");
        List<Ponto> pontos = new ArrayList<>(amostras);
        for (int i = 0; i < amostras; i++) pontos.add(pontoContorno(2*Math.PI*i/amostras));
        return pontos;
    }
    @Override public double calcularArea() {
        return Math.PI*Math.abs(eixoU.getX()*eixoV.getY() - eixoU.getY()*eixoV.getX());
    }
    @Override public double calcularPerimetro() {
        // Integração de Simpson para perímetro da elipse afim, válida também após cisalhamento.
        int n = 256;
        double passo = 2*Math.PI/n, soma = 0;
        for (int i = 0; i <= n; i++) {
            double t = i*passo;
            double vx = -eixoU.getX()*Math.sin(t)+eixoV.getX()*Math.cos(t);
            double vy = -eixoU.getY()*Math.sin(t)+eixoV.getY()*Math.cos(t);
            soma += (i==0 || i==n ? 1 : i%2==0 ? 2 : 4) * Math.hypot(vx, vy);
        }
        return soma*passo/3;
    }
    @Override public Ponto getCentroide() { return centro; }
    @Override public boolean contemPonto(double px, double py) {
        double a = eixoU.getX(), b = eixoV.getX(), c = eixoU.getY(), d = eixoV.getY();
        double det = a*d-b*c;
        double x = px-centro.getX(), y = py-centro.getY();
        double u = (d*x-b*y)/det, v = (-c*x+a*y)/det;
        return u*u+v*v <= 1.0 + 1e-9;
    }
    @Override public void transladar(double dx, double dy) { centro = centro.somar(dx, dy); }
    @Override public void escalar(double fator) {
        Ponto.validarNumero(fator, "Fator");
        if (fator <= 0) throw new IllegalArgumentException("A escala deve ser positiva.");
        eixoU = new Ponto(eixoU.getX()*fator, eixoU.getY()*fator);
        eixoV = new Ponto(eixoV.getX()*fator, eixoV.getY()*fator);
    }
    @Override public void rotacionar(double anguloRadianos) {
        Ponto.validarNumero(anguloRadianos, "Ângulo");
        double co = Math.cos(anguloRadianos), si = Math.sin(anguloRadianos);
        eixoU = girar(eixoU, co, si);
        eixoV = girar(eixoV, co, si);
    }
    private static Ponto girar(Ponto p, double co, double si) {
        return new Ponto(co*p.getX()-si*p.getY(), si*p.getX()+co*p.getY());
    }
    @Override public void cisalhar(double shx, double shy) {
        Ponto.validarNumero(shx, "shx"); Ponto.validarNumero(shy, "shy");
        if (Math.abs(1-shx*shy) < 1e-8) throw new IllegalArgumentException("Cisalhamento degenerado.");
        eixoU = new Ponto(eixoU.getX()+shx*eixoU.getY(), eixoU.getY()+shy*eixoU.getX());
        eixoV = new Ponto(eixoV.getX()+shx*eixoV.getY(), eixoV.getY()+shy*eixoV.getX());
    }
    @Override public Circulo clonar() {
        Circulo copia = new Circulo(getId(), centro, eixoU, eixoV);
        copiarAtributosPara(copia);
        return copia;
    }
}
