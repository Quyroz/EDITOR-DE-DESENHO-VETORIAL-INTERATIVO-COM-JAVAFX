package org.example.Views;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.Controller.EditorContext;
import org.example.Controller.ferramentas.*;
import org.example.Models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Painel funcional: ferramentas, estilo, grade, geometria e camadas. */
public final class BarraLateralView extends ScrollPane {
    private final EditorContext editor;
    private final List<ToggleButton> botoesFerramenta = new ArrayList<>();
    private final ColorPicker preenchimento = new ColorPicker(Color.web("#A5B4FC"));
    private final ColorPicker borda = new ColorPicker(Color.web("#334155"));
    private final Spinner<Double> espessura = new Spinner<>(0.0,30.0,2.0,0.5);
    private final CheckBox snap = new CheckBox("Ajustar à grade (snap)");
    private final CheckBox grade = new CheckBox("Exibir grade");
    private final Spinner<Integer> passo = new Spinner<>(5,100,20,5);
    private final ListView<Camada> listaCamadas = new ListView<>();
    private final Label informacaoSelecao = new Label("Nenhuma forma selecionada");
    private final Label indicacao = new Label("Polígono: clique nos vértices e dê duplo clique para fechar.");
    private boolean atualizando;
    private String ultimoEstadoCamadas = "";
    private String ultimaSelecao = "";

    public BarraLateralView(EditorContext editor) {
        this.editor = editor;
        getStyleClass().add("lateral-scroll");
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setPrefWidth(300);
        setMinWidth(275);
        VBox conteudo = new VBox(14);
        conteudo.getStyleClass().add("sidebar");
        conteudo.setPadding(new Insets(18,14,26,14));
        setContent(conteudo);

        Label titulo = new Label("VETOR STUDIO");
        titulo.getStyleClass().add("sidebar-title");
        Label subtitulo = new Label("Editor gráfico • JavaFX");
        subtitulo.getStyleClass().add("sidebar-subtitle");
        conteudo.getChildren().addAll(titulo, subtitulo, new Separator(), cabecalho("Ferramentas"));
        ToggleGroup ferramentas = new ToggleGroup();
        criarFerramenta(conteudo, ferramentas, "Selecionar", new FerramentaSelecionar(), true);
        criarFerramenta(conteudo, ferramentas, "Mover", new FerramentaTransformar(), false);
        criarFerramenta(conteudo, ferramentas, "Vértices", new FerramentaEditarVertice(), false);
        criarFerramenta(conteudo, ferramentas, "Polígono livre", new FerramentaDesenhar(), false);
        criarFerramenta(conteudo, ferramentas, "Círculo", new FerramentaInserirForma(FerramentaInserirForma.Tipo.CIRCULO), false);
        criarFerramenta(conteudo, ferramentas, "Quadrado", new FerramentaInserirForma(FerramentaInserirForma.Tipo.QUADRADO), false);
        criarFerramenta(conteudo, ferramentas, "Hexágono", new FerramentaInserirForma(FerramentaInserirForma.Tipo.HEXAGONO), false);
        indicacao.setWrapText(true);
        indicacao.getStyleClass().add("hint");
        conteudo.getChildren().add(indicacao);

        conteudo.getChildren().addAll(new Separator(),cabecalho("Aparência"),campo("Preenchimento", preenchimento),campo("Borda", borda));
        espessura.setEditable(true); espessura.setMaxWidth(Double.MAX_VALUE);
        conteudo.getChildren().add(campo("Espessura da borda",espessura));
        preenchimento.setMaxWidth(Double.MAX_VALUE); borda.setMaxWidth(Double.MAX_VALUE);
        preenchimento.valueProperty().addListener((obs,o,n)->aplicarEstilo());
        borda.valueProperty().addListener((obs,o,n)->aplicarEstilo());
        espessura.valueProperty().addListener((obs,o,n)->aplicarEstilo());

        conteudo.getChildren().addAll(new Separator(),cabecalho("Grade"));
        snap.setSelected(editor.getDocumento().isSnapAtivo());
        grade.setSelected(editor.getDocumento().isMostrarGrade());
        passo.setEditable(true);
        snap.selectedProperty().addListener((obs,o,n)->{ if (!atualizando) executarSeguro(() -> editor.executar(d -> d.setSnapAtivo(n))); });
        grade.selectedProperty().addListener((obs,o,n)->{ if (!atualizando) executarSeguro(() -> editor.executar(d -> d.setMostrarGrade(n))); });
        passo.valueProperty().addListener((obs,o,n)->{ if (!atualizando && n != null) executarSeguro(() -> editor.executar(d -> d.setPassoGrade(n))); });
        conteudo.getChildren().addAll(snap, grade, campo("Espaçamento (px)",passo),new Separator(),cabecalho("Edição e transformações"));
        informacaoSelecao.setWrapText(true);
        informacaoSelecao.getStyleClass().add("selection-info");
        conteudo.getChildren().add(informacaoSelecao);
        conteudo.getChildren().add(linhaBotoes(botao("Duplicar", () -> editor.duplicarSelecionadas()),
                                               botao("Excluir", () -> editor.excluirSelecionadas())));
        conteudo.getChildren().add(linhaBotoes(botao("Agrupar", () -> editor.agruparSelecionadas()),
                                               botao("Desagrupar", () -> editor.desagruparSelecionadas())));
        conteudo.getChildren().add(linhaBotoes(botao("Escala", this::dialogoEscala),
                                               botao("Rotação", this::dialogoRotacao)));
        conteudo.getChildren().add(botaoLargura("Cisalhamento", this::dialogoCisalhamento));

        conteudo.getChildren().addAll(new Separator(),cabecalho("Camadas"));
        listaCamadas.setPrefHeight(145);
        listaCamadas.setMaxHeight(175);
        listaCamadas.setCellFactory(lista -> new ListCell<>() {
            @Override protected void updateItem(Camada item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                CheckBox visivel = new CheckBox("◉");
                visivel.setSelected(item.isVisivel());
                visivel.setTooltip(new Tooltip("Camada visível"));
                visivel.setOnAction(e -> executarSeguro(() -> editor.executar(d -> d.getCamada(item.getId()).setVisivel(visivel.isSelected()))));
                CheckBox bloqueada = new CheckBox("Travar");
                bloqueada.setSelected(item.isBloqueada());
                bloqueada.setTooltip(new Tooltip("Bloquear edição"));
                bloqueada.setOnAction(e -> executarSeguro(() -> editor.executar(d -> d.getCamada(item.getId()).setBloqueada(bloqueada.isSelected()))));
                Label nome = new Label(item.getNome());
                nome.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(nome, Priority.ALWAYS);
                HBox linha = new HBox(6,visivel,nome,bloqueada);
                linha.setAlignment(Pos.CENTER_LEFT);
                setGraphic(linha); setText(null);
            }
        });
        listaCamadas.getSelectionModel().selectedItemProperty().addListener((obs,o,n)-> {
            if (n != null && !atualizando && editor.getDocumento().getCamada(n.getId()) != null)
                editor.setCamadaAtiva(n.getId());
        });
        conteudo.getChildren().add(listaCamadas);
        conteudo.getChildren().add(linhaBotoes(botao("+ Camada",this::adicionarCamada),botao("Renomear",this::renomearCamada)));
        conteudo.getChildren().add(linhaBotoes(botao("Remover",this::removerCamada),botao("↑ Subir",()->moverCamada(1))));
        conteudo.getChildren().add(botaoLargura("↓ Descer",()->moverCamada(-1)));
        atualizar();
    }
    private Label cabecalho(String texto) { Label label = new Label(texto); label.getStyleClass().add("section-title"); return label; }
    private VBox campo(String legenda, Node controle) {
        Label titulo = new Label(legenda);
        titulo.getStyleClass().add("field-label");
        if (controle instanceof Control c) c.setMaxWidth(Double.MAX_VALUE);
        return new VBox(5, titulo,controle);
    }
    private HBox linhaBotoes(Button a, Button b) {
        HBox linha = new HBox(7,a,b);
        HBox.setHgrow(a,Priority.ALWAYS); HBox.setHgrow(b,Priority.ALWAYS);
        a.setMaxWidth(Double.MAX_VALUE); b.setMaxWidth(Double.MAX_VALUE);
        return linha;
    }
    private Button botao(String nome, Runnable acao) {
        Button b = new Button(nome);
        b.getStyleClass().add("action-button");
        b.setOnAction(e -> executarSeguro(acao));
        return b;
    }
    private Button botaoLargura(String nome, Runnable acao) {
        Button b = botao(nome,acao); b.setMaxWidth(Double.MAX_VALUE); return b;
    }
    private void criarFerramenta(VBox pai, ToggleGroup grupo, String rotulo,
                                org.example.Controller.Ferramenta ferramenta, boolean selecionada) {
        ToggleButton botao = new ToggleButton(rotulo);
        botao.setToggleGroup(grupo);
        botao.getStyleClass().add("tool-button");
        botao.setMaxWidth(Double.MAX_VALUE);
        botao.setSelected(selecionada);
        botao.setOnAction(e -> {
            if (!botao.isSelected()) { botao.setSelected(true); return; }
            editor.trocarFerramenta(ferramenta);
            indicacao.setText(switch (rotulo) {
                case "Polígono livre" -> "Clique em 3 ou mais vértices; duplo clique conclui. Botão direito cancela.";
                case "Mover" -> "Arraste uma forma ou grupo para mover. Ctrl+clique seleciona múltiplos objetos.";
                case "Vértices" -> "Arraste os quadradinhos azuis; quadrados e hexágonos viram polígonos livres.";
                case "Círculo", "Quadrado", "Hexágono" -> "Clique e arraste na área de desenho para criar a forma.";
                default -> "Clique para selecionar; Ctrl+clique adiciona ou remove da seleção.";
            });
        });
        botoesFerramenta.add(botao); pai.getChildren().add(botao);
    }
    private void aplicarEstilo() {
        if (atualizando || preenchimento.getValue() == null || borda.getValue() == null) return;
        String fill = toHex(preenchimento.getValue()), stroke = toHex(borda.getValue());
        double width = espessura.getValue();
        executarSeguro(() -> {
            editor.setEstiloAtual(fill, stroke, width);
            if (!editor.formasSelecionadas().isEmpty()) editor.executar(d -> {
                for (FormaGeometrica f : editor.formasSelecionadas()) {
                    f.setCorPreenchimentoHex(fill);
                    f.setCorBordaHex(stroke);
                    f.setEspessuraBorda(width);
                }
            });
        });
    }
    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X", (int)Math.round(c.getRed()*255),
                (int)Math.round(c.getGreen()*255), (int)Math.round(c.getBlue()*255));
    }
    private void adicionarCamada() {
        editor.executar(d -> d.adicionarCamada(new Camada("Camada " + (d.getCamadas().size()+1))));
        editor.setCamadaAtiva(editor.getDocumento().getCamadas().get(editor.getDocumento().getCamadas().size()-1).getId());
    }
    private void renomearCamada() {
        Camada atual = editor.getDocumento().getCamada(editor.getCamadaAtiva());
        if (atual == null) return;
        TextInputDialog dialogo = new TextInputDialog(atual.getNome());
        dialogo.setTitle("Renomear camada"); dialogo.setHeaderText("Nome da camada");
        dialogo.showAndWait().ifPresent(nome -> editor.executar(d -> d.getCamada(atual.getId()).setNome(nome)));
    }
    private void removerCamada() {
        String id = editor.getCamadaAtiva();
        editor.executar(d -> d.removerCamada(id));
        editor.setCamadaAtiva(editor.getDocumento().getIdCamadaInicial());
    }
    private void moverCamada(int passo) { editor.executar(d -> d.moverCamada(editor.getCamadaAtiva(),passo)); }
    private Optional<Double> pedirNumero(String titulo, String rotulo, String padrao) {
        TextInputDialog d = new TextInputDialog(padrao);
        d.setTitle(titulo); d.setHeaderText(rotulo);
        return d.showAndWait().map(texto -> Double.parseDouble(texto.trim().replace(',', '.')));
    }
    private void dialogoEscala() { pedirNumero("Escala", "Fator positivo (ex.: 1,5)", "1.5").ifPresent(editor::escalarSelecionadas); }
    private void dialogoRotacao() { pedirNumero("Rotação", "Ângulo em graus (ex.: 45)", "45")
            .ifPresent(graus -> editor.rotacionarSelecionadas(Math.toRadians(graus))); }
    private void dialogoCisalhamento() {
        Optional<Double> x = pedirNumero("Cisalhamento", "Fator horizontal shx", "0.2");
        if (x.isEmpty()) return;
        pedirNumero("Cisalhamento", "Fator vertical shy", "0").ifPresent(y -> editor.cisalharSelecionadas(x.get(), y));
    }
    private void executarSeguro(Runnable tarefa) {
        try { tarefa.run(); }
        catch (Exception ex) {
            Alert aviso = new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK);
            aviso.setHeaderText("Não foi possível executar a ação"); aviso.showAndWait();
            atualizar();
        }
    }
    public void atualizar() {
        DocumentoDesenho d = editor.getDocumento();
        String estadoCamadas = d.getCamadas().stream().map(c -> c.getId() + c.getNome() + c.isVisivel() + c.isBloqueada())
                .reduce("", String::concat) + editor.getCamadaAtiva();
        if (!estadoCamadas.equals(ultimoEstadoCamadas)) {
            ultimoEstadoCamadas = estadoCamadas;
            atualizando = true;
            List<Camada> ordenadas = new ArrayList<>(d.getCamadas());
            java.util.Collections.reverse(ordenadas);
            listaCamadas.setItems(FXCollections.observableArrayList(ordenadas));
            d.getCamadas().stream().filter(c->c.getId().equals(editor.getCamadaAtiva()))
                    .findFirst().ifPresent(c -> listaCamadas.getSelectionModel().select(c));
            atualizando = false;
        }
        String selecaoAtual = editor.getSelecionados().toString();
        List<FormaGeometrica> formas = editor.formasSelecionadas();
        if (formas.isEmpty()) informacaoSelecao.setText("Nenhuma forma selecionada");
        else {
            double area = formas.stream().mapToDouble(FormaGeometrica::calcularArea).sum();
            double perimetro = formas.stream().mapToDouble(FormaGeometrica::calcularPerimetro).sum();
            informacaoSelecao.setText(String.format(java.util.Locale.forLanguageTag("pt-BR"),
                    "%d forma(s) · Área: %.1f px² · Perímetro: %.1f px",formas.size(),area,perimetro));
            if (!selecaoAtual.equals(ultimaSelecao)) {
                FormaGeometrica primeira = formas.get(0);
                atualizando = true;
                preenchimento.setValue(Color.web(primeira.getCorPreenchimentoHex()));
                borda.setValue(Color.web(primeira.getCorBordaHex()));
                espessura.getValueFactory().setValue(primeira.getEspessuraBorda());
                atualizando = false;
            }
        }
        ultimaSelecao = selecaoAtual;
        atualizando = true;
        if (snap.isSelected() != d.isSnapAtivo()) snap.setSelected(d.isSnapAtivo());
        if (grade.isSelected() != d.isMostrarGrade()) grade.setSelected(d.isMostrarGrade());
        if (!passo.getValue().equals(d.getPassoGrade())) passo.getValueFactory().setValue(d.getPassoGrade());
        atualizando = false;
    }
}
