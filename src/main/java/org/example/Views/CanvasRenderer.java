package org.example.Views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.Controller.EditorContext;
import org.example.Controller.ferramentas.FerramentaDesenhar;
import org.example.Controller.ferramentas.FerramentaInserirForma;
import org.example.Models.*;

import java.util.List;

/** Camada de apresentação: renderização sem lógica de persistência ou histórico. */
public final class CanvasRenderer {
    public void renderizar(Canvas canvas, EditorContext editor) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        DocumentoDesenho documento = editor.getDocumento();
        double w = canvas.getWidth(), h = canvas.getHeight();
        g.setFill(Color.WHITE); g.fillRect(0, 0, w, h);
        if (documento.isMostrarGrade()) desenharGrade(g, w, h, documento.getPassoGrade());
        for (FormaGeometrica f : documento.formasOrdenadas()) {
            Camada c = documento.getCamada(f.getLayerId());
            if (c == null || !c.isVisivel()) continue;
            desenharForma(g, f, editor.getSelecionados().contains(f.getId()));
            if (editor.getSelecionados().contains(f.getId()) && f instanceof Poligono) desenharVertices(g, f.getPontos());
        }
        if (editor.getFerramenta() instanceof FerramentaDesenhar ferramenta) {
            List<Ponto> vertices = ferramenta.getVertices();
            if (!vertices.isEmpty()) {
                g.save();
                g.setLineWidth(2); g.setStroke(Color.web("#2563EB"));
                g.setLineDashes(5,5);
                for (int i=1;i<vertices.size();i++) g.strokeLine(vertices.get(i-1).getX(), vertices.get(i-1).getY(), vertices.get(i).getX(),vertices.get(i).getY());
                Ponto cursor = ferramenta.getCursor();
                if (cursor != null) {
                    Ponto ultimo = vertices.get(vertices.size()-1);
                    g.strokeLine(ultimo.getX(), ultimo.getY(), cursor.getX(), cursor.getY());
                    if (vertices.size()>1) g.strokeLine(cursor.getX(),cursor.getY(),vertices.get(0).getX(),vertices.get(0).getY());
                }
                g.setLineDashes();
                desenharVertices(g, vertices);
                g.restore();
            }
        }
        if (editor.getFerramenta() instanceof FerramentaInserirForma ferramenta && ferramenta.getInicio() != null) {
            desenharPrevia(g, ferramenta.getTipo(), ferramenta.getInicio(), ferramenta.getFim());
        }
    }

    private void desenharGrade(GraphicsContext g, double w, double h, int passo) {
        g.save();
        for (int x=0; x<=w; x+=passo) {
            g.setStroke(x%(passo*5)==0 ? Color.web("#E2E8F0") : Color.web("#F1F5F9"));
            g.strokeLine(x+0.5, 0, x+0.5, h);
        }
        for (int y=0; y<=h; y+=passo) {
            g.setStroke(y%(passo*5)==0 ? Color.web("#E2E8F0") : Color.web("#F1F5F9"));
            g.strokeLine(0, y+0.5, w, y+0.5);
        }
        g.restore();
    }
    private void desenharForma(GraphicsContext g, FormaGeometrica f, boolean selecionado) {
        g.save();
        List<Ponto> vertices = f instanceof Circulo c ? c.amostrarContorno(144) : f.getPontos();
        if (vertices.size() < 3) { g.restore(); return; }
        double[] xs = vertices.stream().mapToDouble(Ponto::getX).toArray();
        double[] ys = vertices.stream().mapToDouble(Ponto::getY).toArray();
        g.setFill(Color.web(f.getCorPreenchimentoHex()));
        g.fillPolygon(xs,ys,vertices.size());
        if (f.getEspessuraBorda() > 0) {
            g.setStroke(Color.web(f.getCorBordaHex()));
            g.setLineWidth(f.getEspessuraBorda());
            g.strokePolygon(xs,ys,vertices.size());
        }
        if (selecionado) {
            g.setStroke(Color.web("#2563EB"));
            g.setLineWidth(2.5);
            g.setLineDashes(8,4);
            g.strokePolygon(xs,ys,vertices.size());
        }
        g.restore();
    }
    private void desenharVertices(GraphicsContext g, List<Ponto> pontos) {
        g.save(); g.setLineDashes();
        for (Ponto p : pontos) {
            g.setFill(Color.WHITE); g.setStroke(Color.web("#2563EB")); g.setLineWidth(1.5);
            g.fillRect(p.getX()-4,p.getY()-4,8,8);
            g.strokeRect(p.getX()-4,p.getY()-4,8,8);
        }
        g.restore();
    }
    private void desenharPrevia(GraphicsContext g, FerramentaInserirForma.Tipo tipo, Ponto inicio, Ponto fim) {
        if (fim == null) return;
        double dx = fim.getX()-inicio.getX(), dy=fim.getY()-inicio.getY();
        double tamanho = Math.hypot(dx,dy);
        if (tamanho < 1) return;
        try {
            FormaGeometrica previa = switch (tipo) {
                case CIRCULO -> new Circulo(inicio,tamanho);
                case HEXAGONO -> new Hexagono(inicio,tamanho);
                case QUADRADO -> {
                    double lado=Math.max(Math.abs(dx),Math.abs(dy));
                    yield new Quadrado(new Ponto(dx < 0 ? inicio.getX()-lado : inicio.getX(),
                                                 dy < 0 ? inicio.getY()-lado : inicio.getY()), lado);
                }
            };
            previa.setCorPreenchimentoHex("#BFDBFE77");
            previa.setCorBordaHex("#2563EB");
            previa.setEspessuraBorda(2);
            desenharForma(g, previa, false);
        } catch (IllegalArgumentException ignored) { /* cursor ainda no ponto inicial */ }
    }
}
