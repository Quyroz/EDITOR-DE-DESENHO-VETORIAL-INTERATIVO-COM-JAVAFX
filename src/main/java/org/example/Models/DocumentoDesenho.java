package org.example.Models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Agregado raiz: formas, camadas e configuração do canvas. */
public final class DocumentoDesenho {
    private final List<FormaGeometrica> formas = new ArrayList<>();
    private final List<Camada> camadas = new ArrayList<>();
    private double largura = 1400;
    private double altura = 900;
    private int passoGrade = 20;
    private boolean snapAtivo = true;
    private boolean mostrarGrade = true;

    public DocumentoDesenho() { camadas.add(new Camada("Camada 1")); }
    public List<FormaGeometrica> getFormas() { return List.copyOf(formas); }
    public List<Camada> getCamadas() { return List.copyOf(camadas); }
    public double getLargura() { return largura; }
    public double getAltura() { return altura; }
    public int getPassoGrade() { return passoGrade; }
    public boolean isSnapAtivo() { return snapAtivo; }
    public boolean isMostrarGrade() { return mostrarGrade; }
    public void setLargura(double largura) {
        Ponto.validarNumero(largura, "Largura");
        if (largura < 100 || largura > 10000) throw new IllegalArgumentException("Largura deve estar entre 100 e 10000.");
        this.largura = largura;
    }
    public void setAltura(double altura) {
        Ponto.validarNumero(altura, "Altura");
        if (altura < 100 || altura > 10000) throw new IllegalArgumentException("Altura deve estar entre 100 e 10000.");
        this.altura = altura;
    }
    public void setPassoGrade(int passoGrade) {
        if (passoGrade < 5 || passoGrade > 200) throw new IllegalArgumentException("Grade deve ter passo entre 5 e 200.");
        this.passoGrade = passoGrade;
    }
    public void setSnapAtivo(boolean snapAtivo) { this.snapAtivo = snapAtivo; }
    public void setMostrarGrade(boolean mostrarGrade) { this.mostrarGrade = mostrarGrade; }
    public Camada getCamada(String id) {
        return camadas.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
    public String getIdCamadaInicial() { return camadas.get(0).getId(); }
    public void adicionarCamada(Camada camada) {
        if (camada == null || getCamada(camada.getId()) != null) throw new IllegalArgumentException("Camada inválida ou duplicada.");
        camadas.add(camada);
    }
    public void removerCamada(String id) {
        if (camadas.size() == 1) throw new IllegalStateException("Deve existir ao menos uma camada.");
        Camada camada = getCamada(id);
        if (camada == null) throw new IllegalArgumentException("Camada inexistente.");
        String destino = camadas.stream().filter(c -> !c.getId().equals(id)).findFirst().orElseThrow().getId();
        for (FormaGeometrica f : formas) if (f.getLayerId().equals(id)) f.setLayerId(destino);
        camadas.remove(camada);
    }
    public void moverCamada(String id, int deslocamento) {
        Camada c = getCamada(id);
        if (c == null) return;
        int atual = camadas.indexOf(c), prox = atual + deslocamento;
        if (prox < 0 || prox >= camadas.size()) return;
        camadas.remove(atual);
        camadas.add(prox, c);
    }
    public void adicionarForma(FormaGeometrica forma) {
        if (forma == null || forma.getLayerId() == null || getCamada(forma.getLayerId()) == null)
            throw new IllegalArgumentException("Forma sem camada válida.");
        if (buscarForma(forma.getId()) != null) throw new IllegalArgumentException("ID da forma duplicado.");
        formas.add(forma);
    }
    public FormaGeometrica buscarForma(String id) {
        return formas.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
    }
    public void removerForma(String id) { formas.removeIf(f -> f.getId().equals(id)); }
    public void substituirForma(String id, FormaGeometrica nova) {
        if (!id.equals(nova.getId())) throw new IllegalArgumentException("Identidade da forma alterada.");
        for (int i = 0; i < formas.size(); i++) {
            if (formas.get(i).getId().equals(id)) { formas.set(i, nova); return; }
        }
        throw new IllegalArgumentException("Forma inexistente.");
    }
    public Ponto aplicarSnap(Ponto ponto) {
        if (!snapAtivo) return ponto;
        return new Ponto(Math.round(ponto.getX()/passoGrade)*passoGrade,
                         Math.round(ponto.getY()/passoGrade)*passoGrade);
    }
    /** Ordenação ascendente de camadas; formas posteriores são desenhadas acima. */
    public List<FormaGeometrica> formasOrdenadas() {
        List<FormaGeometrica> ordenadas = new ArrayList<>(formas);
        ordenadas.sort(java.util.Comparator.comparingInt(f -> indiceCamada(f.getLayerId())));
        return ordenadas;
    }
    private int indiceCamada(String id) {
        for (int i=0; i<camadas.size(); i++) if (camadas.get(i).getId().equals(id)) return i;
        return -1;
    }
    public void validarIntegridade() {
        if (camadas.isEmpty()) throw new IllegalArgumentException("O documento não tem camadas.");
        Set<String> ids = new HashSet<>();
        for (Camada c : camadas) if (!ids.add(c.getId())) throw new IllegalArgumentException("Camadas duplicadas.");
        ids.clear();
        for (FormaGeometrica f : formas) {
            if (!ids.add(f.getId())) throw new IllegalArgumentException("Formas duplicadas.");
            if (getCamada(f.getLayerId()) == null) throw new IllegalArgumentException("Forma com camada inexistente.");
        }
    }
    public DocumentoDesenho clonar() {
        DocumentoDesenho copia = new DocumentoDesenho();
        copia.camadas.clear();
        for (Camada c : camadas) copia.camadas.add(c.clonar());
        for (FormaGeometrica f : formas) copia.formas.add(f.clonar());
        copia.largura = largura; copia.altura = altura;
        copia.passoGrade = passoGrade; copia.snapAtivo = snapAtivo; copia.mostrarGrade = mostrarGrade;
        return copia;
    }
}
