package org.example.Views;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Controller.EditorContext;
import org.example.Controller.ferramentas.FerramentaDesenhar;
import org.example.Models.DocumentoDesenho;
import org.example.Models.FormaGeometrica;
import org.example.Persistence.ArquivoProjeto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/** Aplicação JavaFX (View). Coordenações da interface passam por EditorContext. */
public final class ContainerApp extends Application {
    private final EditorContext editor = new EditorContext();
    private final ArquivoProjeto armazenamento = new ArquivoProjeto();
    private final CanvasRenderer renderizador = new CanvasRenderer();
    private BarraLateralView lateral;
    private Canvas canvas;
    private Label status;
    private Label estadoArquivo;
    private Stage janela;
    private Path arquivoAtual;

    @Override public void start(Stage stage) {
        janela = stage;
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("app-root");
        lateral = new BarraLateralView(editor);
        layout.setLeft(lateral);
        canvas = new Canvas(editor.getDocumento().getLargura(), editor.getDocumento().getAltura());
        configurarMouse();
        StackPane folha = new StackPane(canvas);
        folha.getStyleClass().add("canvas-sheet");
        StackPane.setAlignment(canvas, Pos.TOP_LEFT);
        ScrollPane rolador = new ScrollPane(folha);
        rolador.setPannable(false);
        rolador.setFitToWidth(false);
        rolador.setFitToHeight(false);
        rolador.getStyleClass().add("canvas-scroll");
        layout.setCenter(rolador);

        VBox superior = new VBox(criarMenu(), criarBarraAcoes());
        layout.setTop(superior);
        layout.setBottom(criarStatus());
        editor.setAoMudar(this::atualizarTela);
        Scene cena = new Scene(layout,1250,780);
        var css = getClass().getResource("/org/example/Views/styles.css");
        if (css != null) cena.getStylesheets().add(css.toExternalForm());
        stage.setScene(cena);
        stage.setTitle("Vetor Studio · Editor Vetorial");
        stage.setMinWidth(860); stage.setMinHeight(580);
        stage.setOnCloseRequest(evento -> {
            if (!confirmarPerdaDeAlteracoes()) evento.consume();
        });
        atualizarTela();
        stage.show();
    }

    private MenuBar criarMenu() {
        Menu arquivo = new Menu("Arquivo");
        arquivo.getItems().addAll(item("Novo", "Shortcut+N", this::novo),
                item("Abrir...", "Shortcut+O", this::abrir),
                item("Salvar", "Shortcut+S", this::salvar),
                item("Salvar como...", "Shortcut+Shift+S", this::salvarComo),
                new SeparatorMenuItem(),item("Sair",null,()->janela.fireEvent(new javafx.stage.WindowEvent(janela, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST))));
        Menu editar = new Menu("Editar");
        editar.getItems().addAll(item("Desfazer", "Shortcut+Z", editor::desfazer),
                item("Refazer", "Shortcut+Y", editor::refazer),
                new SeparatorMenuItem(), item("Duplicar", "Shortcut+D", editor::duplicarSelecionadas),
                item("Excluir", "DELETE", editor::excluirSelecionadas),
                item("Agrupar", "Shortcut+G", editor::agruparSelecionadas),
                item("Desagrupar", "Shortcut+Shift+G", editor::desagruparSelecionadas));
        Menu ajuda = new Menu("Ajuda");
        ajuda.getItems().add(item("Como usar",null,this::instrucoes));
        return new MenuBar(arquivo,editar,ajuda);
    }
    private MenuItem item(String titulo, String atalho, Runnable acao) {
        MenuItem menu = new MenuItem(titulo);
        if (atalho != null) menu.setAccelerator(KeyCombination.keyCombination(atalho));
        menu.setOnAction(e->executarSeguro(acao));
        return menu;
    }
    private ToolBar criarBarraAcoes() {
        Label logotipo = new Label("✦  VETOR STUDIO");
        logotipo.getStyleClass().add("brand");
        Button novo=acao("Novo",this::novo), abrir=acao("Abrir",this::abrir);
        Button salvar=acao("Salvar",this::salvar), desfazer=acao("↶ Desfazer",editor::desfazer);
        Button refazer=acao("↷ Refazer",editor::refazer), duplicar=acao("Duplicar",editor::duplicarSelecionadas);
        return new ToolBar(logotipo,new Separator(),novo,abrir,salvar,new Separator(),desfazer,refazer,new Separator(),duplicar);
    }
    private Button acao(String nome,Runnable runnable) {
        Button botao = new Button(nome);
        botao.getStyleClass().add("toolbar-button");
        botao.setOnAction(e->executarSeguro(runnable));
        return botao;
    }
    private HBox criarStatus() {
        status = new Label("Pronto");
        estadoArquivo = new Label("Sem alterações");
        HBox.setHgrow(status,Priority.ALWAYS);
        status.setMaxWidth(Double.MAX_VALUE);
        HBox barra = new HBox(12,status,estadoArquivo);
        barra.getStyleClass().add("status-bar");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(9,15,9,15));
        return barra;
    }
    private void configurarMouse() {
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> executarSeguro(() -> editor.getFerramenta().aoPressionarMouse(editor,e)));
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            executarSeguro(() -> editor.getFerramenta().aoArrastarMouse(editor,e));
            editor.atualizarMouse(e.getX(),e.getY());
        });
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> executarSeguro(() -> editor.getFerramenta().aoSoltarMouse(editor,e)));
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> executarSeguro(() -> editor.getFerramenta().aoClicar(editor,e)));
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            if (editor.getFerramenta() instanceof FerramentaDesenhar f) f.atualizarCursor(editor,e.getX(),e.getY());
            editor.atualizarMouse(e.getX(),e.getY());
        });
    }
    private void atualizarTela() {
        DocumentoDesenho documento = editor.getDocumento();
        if (canvas == null || status == null || lateral == null) return;
        if (canvas.getWidth() != documento.getLargura()) canvas.setWidth(documento.getLargura());
        if (canvas.getHeight() != documento.getAltura()) canvas.setHeight(documento.getAltura());
        renderizador.renderizar(canvas,editor);
        lateral.atualizar();
        int total = documento.getFormas().size();
        var selecionadas = editor.formasSelecionadas();
        double area = selecionadas.stream().mapToDouble(FormaGeometrica::calcularArea).sum();
        double perimetro = selecionadas.stream().mapToDouble(FormaGeometrica::calcularPerimetro).sum();
        status.setText(String.format(Locale.forLanguageTag("pt-BR"),
                "Cursor: (%.0f, %.0f)  |  %d formas  |  Selecionadas: %d  |  Área: %.1f px²  |  Perímetro: %.1f px  |  %s",
                editor.getMouseX(),editor.getMouseY(),total,selecionadas.size(),area,perimetro,editor.getMensagem()));
        estadoArquivo.setText(editor.isModificado() ? "● Alterações não salvas" : "✓ Salvo");
        janela.setTitle((arquivoAtual == null ? "Sem título" : arquivoAtual.getFileName())
                + (editor.isModificado() ? " *" : "") + " · Vetor Studio");
    }
    private void novo() {
        if (!confirmarPerdaDeAlteracoes()) return;
        editor.novoDocumento(); arquivoAtual = null; editor.setMensagem("Novo documento iniciado");
    }
    private void abrir() {
        if (!confirmarPerdaDeAlteracoes()) return;
        FileChooser seletor = seletorArquivos("Abrir projeto vetorial");
        File escolhido = seletor.showOpenDialog(janela);
        if (escolhido == null) return;
        try {
            DocumentoDesenho carregado = armazenamento.carregar(escolhido.toPath());
            editor.carregarDocumento(carregado);
            arquivoAtual = escolhido.toPath();
            editor.setMensagem("Projeto aberto com sucesso");
        } catch (IOException ex) { erro("Não foi possível abrir o projeto",ex); }
    }
    private boolean salvar() {
        if (arquivoAtual == null) return salvarComo();
        try {
            armazenamento.salvar(arquivoAtual,editor.getDocumento());
            editor.marcarSalvo(); editor.setMensagem("Projeto salvo: " + arquivoAtual.getFileName());
            return true;
        } catch (IOException ex) { erro("Falha ao salvar o projeto",ex); return false; }
    }
    private boolean salvarComo() {
        FileChooser seletor = seletorArquivos("Salvar projeto vetorial");
        seletor.setInitialFileName(arquivoAtual == null ? "meu-desenho.json" : arquivoAtual.getFileName().toString());
        File escolhido = seletor.showSaveDialog(janela);
        if (escolhido == null) return false;
        Path original = arquivoAtual;
        String nome = escolhido.getName().toLowerCase(Locale.ROOT);
        arquivoAtual = nome.endsWith(".json") ? escolhido.toPath() : escolhido.toPath().resolveSibling(escolhido.getName()+".json");
        if (!salvar()) { arquivoAtual = original; return false; }
        return true;
    }
    private FileChooser seletorArquivos(String titulo) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle(titulo);
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Projeto JSON (*.json)","*.json"));
        return seletor;
    }
    private boolean confirmarPerdaDeAlteracoes() {
        if (!editor.isModificado()) return true;
        ButtonType salvar = new ButtonType("Salvar",ButtonBar.ButtonData.YES);
        ButtonType descartar = new ButtonType("Descartar",ButtonBar.ButtonData.NO);
        ButtonType cancelar = new ButtonType("Cancelar",ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION);
        dialogo.setTitle("Alterações não salvas");
        dialogo.setHeaderText("O documento contém alterações não salvas.");
        dialogo.setContentText("Deseja salvar antes de continuar?");
        dialogo.getButtonTypes().setAll(salvar,descartar,cancelar);
        ButtonType resposta = dialogo.showAndWait().orElse(cancelar);
        if (resposta == salvar) return salvar();
        return resposta == descartar;
    }
    private void instrucoes() {
        Alert ajuda = new Alert(Alert.AlertType.INFORMATION);
        ajuda.setTitle("Ajuda — Vetor Studio");
        ajuda.setHeaderText("Como utilizar o editor");
        ajuda.setContentText("1. Escolha uma ferramenta à esquerda.\n"
                + "2. Círculo, quadrado e hexágono: clique e arraste.\n"
                + "3. Polígono: clique nos vértices, duplo clique conclui.\n"
                + "4. Seleção: Ctrl+clique para selecionar múltiplas formas.\n"
                + "5. Mover: arraste; Vértices: arraste os pontos azuis.\n"
                + "6. Edite cores, escala, rotação, camadas e agrupamentos.\n"
                + "7. Ctrl+Z desfaz, Ctrl+Y refaz, Ctrl+S salva.");
        ajuda.showAndWait();
    }
    private void executarSeguro(Runnable acao) {
        try { acao.run(); }
        catch (Exception ex) { erro("Não foi possível realizar a operação",ex); }
    }
    private void erro(String cabecalho,Exception ex) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Editor Vetorial"); alerta.setHeaderText(cabecalho);
        alerta.setContentText(ex.getMessage() == null ? "Erro inesperado." : ex.getMessage());
        alerta.showAndWait();
    }
    public static void main(String[] args) { launch(args); }
}
