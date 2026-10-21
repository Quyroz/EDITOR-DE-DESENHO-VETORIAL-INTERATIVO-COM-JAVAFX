package org.example.Controller;

import org.example.Models.*;
import org.example.Controller.ferramentas.FerramentaSelecionar;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Estado de edição e fachada entre GUI e domínio; Views não manipulam listas internas. */
public final class EditorContext {
    private DocumentoDesenho documento = new DocumentoDesenho();
    private final HistoricoController historico = new HistoricoController();
    private Ferramenta ferramenta = new FerramentaSelecionar();
    private final Set<String> selecionados = new LinkedHashSet<>();
    private String camadaAtiva = documento.getIdCamadaInicial();
    private String corPreenchimento = "#A5B4FC";
    private String corBorda = "#334155";
    private double larguraBorda = 2.0;
    private Runnable aoMudar = () -> { };
    private String mensagem = "Pronto";
    private double mouseX, mouseY;
    private boolean modificado;

    public DocumentoDesenho getDocumento() { return documento; }
    public HistoricoController getHistorico() { return historico; }
    public Ferramenta getFerramenta() { return ferramenta; }
    public String getCamadaAtiva() { return camadaAtiva; }
    public Set<String> getSelecionados() { return Set.copyOf(selecionados); }
    public String getCorPreenchimento() { return corPreenchimento; }
    public String getCorBorda() { return corBorda; }
    public double getLarguraBorda() { return larguraBorda; }
    public String getMensagem() { return mensagem; }
    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }
    public boolean isModificado() { return modificado; }
    public void setAoMudar(Runnable aoMudar) { this.aoMudar = aoMudar == null ? () -> {} : aoMudar; }
    public void notificar() { aoMudar.run(); }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; notificar(); }
    public void atualizarMouse(double x, double y) { mouseX = x; mouseY = y; notificar(); }
    public Ponto pontoMouse(double x, double y) { return documento.aplicarSnap(new Ponto(x, y)); }
    public void setCamadaAtiva(String camadaAtiva) {
        if (documento.getCamada(camadaAtiva) == null) throw new IllegalArgumentException("Camada inexistente.");
        this.camadaAtiva = camadaAtiva; notificar();
    }
    public boolean camadaEditavel() {
        Camada c = documento.getCamada(camadaAtiva);
        return c != null && c.isVisivel() && !c.isBloqueada();
    }
    public void setEstiloAtual(String preenchimento, String borda, double espessura) {
        FormaBase.validarCorHex(preenchimento);
        FormaBase.validarCorHex(borda);
        FormaBase.validarEspessura(espessura);
        this.corPreenchimento = preenchimento;
        this.corBorda = borda;
        this.larguraBorda = espessura;
        notificar();
    }
    public void aplicarEstilo(FormaGeometrica f) {
        f.setCorPreenchimentoHex(corPreenchimento);
        f.setCorBordaHex(corBorda);
        f.setEspessuraBorda(larguraBorda);
        f.setLayerId(camadaAtiva);
    }
    public void trocarFerramenta(Ferramenta proxima) {
        ferramenta.aoDesativar(this);
        ferramenta = proxima;
        mensagem = "Ferramenta: " + proxima.nome();
        notificar();
    }
    public void executar(Comando comando) {
        DocumentoDesenho anterior = documento.clonar();
        try {
            historico.executar(comando, documento);
            reconciliarSelecao();
            modificado = true;
            notificar();
        } catch (RuntimeException ex) {
            documento = anterior;
            notificar();
            throw ex;
        }
    }
    public void registrarArrasto(DocumentoDesenho antes) {
        historico.registrarEstado(antes);
        modificado = true;
        notificar();
    }
    public void desfazer() {
        if (!historico.podeDesfazer()) return;
        documento = historico.desfazer(documento);
        reconciliarSelecao(); modificado = true; notificar();
    }
    public void refazer() {
        if (!historico.podeRefazer()) return;
        documento = historico.refazer(documento);
        reconciliarSelecao(); modificado = true; notificar();
    }
    public void novoDocumento() { carregarDocumento(new DocumentoDesenho()); modificado = false; notificar(); }
    public void carregarDocumento(DocumentoDesenho novo) {
        novo.validarIntegridade();
        ferramenta.aoDesativar(this);
        documento = novo;
        selecionados.clear();
        camadaAtiva = documento.getIdCamadaInicial();
        historico.limpar(); modificado = false;
        mensagem = "Documento carregado"; notificar();
    }
    public void marcarSalvo() { modificado = false; notificar(); }
    private void reconciliarSelecao() {
        selecionados.removeIf(id -> {
            FormaGeometrica f = documento.buscarForma(id);
            if (f == null) return true;
            Camada c = documento.getCamada(f.getLayerId());
            return c == null || !c.isVisivel() || c.isBloqueada();
        });
    }

    public FormaGeometrica localizarForma(double x, double y) {
        List<FormaGeometrica> ordenadas = documento.formasOrdenadas();
        for (int i = ordenadas.size()-1; i>=0; i--) {
            FormaGeometrica f = ordenadas.get(i);
            Camada camada = documento.getCamada(f.getLayerId());
            if (camada != null && camada.isVisivel() && !camada.isBloqueada() && f.contemPonto(x,y)) return f;
        }
        return null;
    }
    /** Ao selecionar membro de grupo, seleção expande-se para o grupo inteiro. */
    public void selecionar(FormaGeometrica alvo, boolean aditivo) {
        if (!aditivo) selecionados.clear();
        if (alvo != null) {
            List<FormaGeometrica> conjunto = membrosDoGrupo(alvo);
            boolean remover = aditivo && selecionados.contains(alvo.getId());
            for (FormaGeometrica f : conjunto) {
                if (remover) selecionados.remove(f.getId()); else selecionados.add(f.getId());
            }
        }
        notificar();
    }
    public List<FormaGeometrica> membrosDoGrupo(FormaGeometrica forma) {
        if (forma.getGroupId() == null) return List.of(forma);
        return documento.getFormas().stream()
                .filter(f -> forma.getGroupId().equals(f.getGroupId()))
                .filter(f -> { Camada c = documento.getCamada(f.getLayerId());
                    return c != null && c.isVisivel() && !c.isBloqueada(); })
                .toList();
    }
    public List<FormaGeometrica> formasSelecionadas() {
        return documento.getFormas().stream()
                .filter(f -> selecionados.contains(f.getId()))
                .filter(f -> { Camada c = documento.getCamada(f.getLayerId());
                    return c != null && c.isVisivel() && !c.isBloqueada(); })
                .toList();
    }
    public void limparSelecao() { selecionados.clear(); notificar(); }
    public void selecionarIds(List<String> ids) { selecionados.clear(); selecionados.addAll(ids); reconciliarSelecao(); notificar(); }
    public void excluirSelecionadas() {
        if (selecionados.isEmpty()) return;
        Set<String> ids = formasSelecionadas().stream().map(FormaGeometrica::getId)
                .collect(java.util.stream.Collectors.toSet());
        if (ids.isEmpty()) { limparSelecao(); return; }
        executar(d -> ids.forEach(d::removerForma));
        limparSelecao();
    }
    public void duplicarSelecionadas() {
        List<FormaGeometrica> originais = formasSelecionadas();
        if (originais.isEmpty()) return;
        List<String> novos = new ArrayList<>();
        java.util.Map<String,String> grupos = new java.util.HashMap<>();
        executar(d -> {
            for (FormaGeometrica f : originais) {
                FormaGeometrica copia = copiarComNovoId(f);
                copia.transladar(30, 30);
                if (f.getGroupId() != null)
                    copia.setGroupId(grupos.computeIfAbsent(f.getGroupId(), k -> UUID.randomUUID().toString()));
                d.adicionarForma(copia);
                novos.add(copia.getId());
            }
        });
        selecionarIds(novos);
    }
    private static FormaGeometrica copiarComNovoId(FormaGeometrica f) {
        FormaGeometrica nova;
        if (f instanceof Circulo c) nova = new Circulo(UUID.randomUUID().toString(), c.getCentro(), c.getEixoU(), c.getEixoV());
        else if (f instanceof Quadrado) nova = new Quadrado(UUID.randomUUID().toString(), f.getPontos());
        else if (f instanceof Hexagono) nova = new Hexagono(UUID.randomUUID().toString(), f.getPontos());
        else nova = new Poligono(UUID.randomUUID().toString(), f.getPontos());
        nova.setCorPreenchimentoHex(f.getCorPreenchimentoHex());
        nova.setCorBordaHex(f.getCorBordaHex());
        nova.setEspessuraBorda(f.getEspessuraBorda());
        nova.setGroupId(f.getGroupId());
        nova.setLayerId(f.getLayerId());
        return nova;
    }
    public void agruparSelecionadas() {
        if (selecionados.size() < 2) throw new IllegalStateException("Selecione pelo menos duas formas (Ctrl+clique).");
        String grupo = UUID.randomUUID().toString();
        executar(d -> formasSelecionadas().forEach(f -> f.setGroupId(grupo)));
    }
    public void desagruparSelecionadas() {
        if (selecionados.isEmpty()) return;
        executar(d -> formasSelecionadas().forEach(f -> f.setGroupId(null)));
    }
    public void escalarSelecionadas(double fator) {
        if (selecionados.isEmpty()) return;
        executar(d -> Transformacoes.escalar(formasSelecionadas(), fator));
    }
    public void rotacionarSelecionadas(double radianos) {
        if (selecionados.isEmpty()) return;
        executar(d -> Transformacoes.rotacionar(formasSelecionadas(), radianos));
    }
    public void cisalharSelecionadas(double shx, double shy) {
        if (selecionados.isEmpty()) return;
        executar(d -> Transformacoes.cisalhar(formasSelecionadas(), shx, shy));
    }
}
