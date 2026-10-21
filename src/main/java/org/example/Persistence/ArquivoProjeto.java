package org.example.Persistence;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.Models.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Formato JSON explícito: versões, validação, tipos conhecidos e gravação atômica. */
public final class ArquivoProjeto {
    private static final int VERSAO = 1;
    private static final int LIMITE_FORMAS = 50_000;
    private static final int LIMITE_CAMADAS = 2_000;
    private static final long LIMITE_BYTES = 30 * 1024 * 1024;
    private final ObjectMapper json = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public void salvar(Path caminho, DocumentoDesenho documento) throws IOException {
        documento.validarIntegridade();
        if (caminho == null) throw new IllegalArgumentException("Caminho de arquivo obrigatório.");
        List<CamadaJson> camadas = documento.getCamadas().stream()
                .map(c -> new CamadaJson(c.getId(), c.getNome(), c.isVisivel(), c.isBloqueada())).toList();
        List<FormaJson> formas = documento.getFormas().stream().map(this::converter).toList();
        DocumentoJson estrutura = new DocumentoJson(VERSAO, documento.getLargura(), documento.getAltura(),
                documento.getPassoGrade(), documento.isSnapAtivo(), documento.isMostrarGrade(), camadas, formas);
        Path destino = caminho.toAbsolutePath().normalize();
        Path pasta = destino.getParent();
        if (pasta == null || !Files.isDirectory(pasta)) throw new IOException("Diretório de destino inexistente.");
        Path temporario = Files.createTempFile(pasta, ".editor-vetorial-", ".tmp");
        try {
            json.writeValue(temporario.toFile(), estrutura);
            try { Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException ex) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporario); }
    }

    public DocumentoDesenho carregar(Path caminho) throws IOException {
        if (caminho == null || !Files.isRegularFile(caminho)) throw new IOException("Arquivo não encontrado.");
        if (Files.size(caminho) > LIMITE_BYTES) throw new IOException("Arquivo excede o tamanho máximo permitido.");
        try {
            DocumentoJson dados = json.readValue(caminho.toFile(), DocumentoJson.class);
            if (dados == null || dados.versao() != VERSAO) throw new IllegalArgumentException("Versão de projeto incompatível.");
            if (dados.camadas() == null || dados.camadas().isEmpty() || dados.camadas().size() > LIMITE_CAMADAS)
                throw new IllegalArgumentException("Lista de camadas inválida.");
            if (dados.formas() == null || dados.formas().size() > LIMITE_FORMAS)
                throw new IllegalArgumentException("Lista de formas inválida.");
            DocumentoDesenho documento = new DocumentoDesenho();
            documento.setLargura(dados.largura());
            documento.setAltura(dados.altura());
            documento.setPassoGrade(dados.passoGrade());
            documento.setSnapAtivo(dados.snapAtivo());
            documento.setMostrarGrade(dados.mostrarGrade());
            // O documento nasce com uma camada padrão: remova-a após criar outra.
            List<Camada> camadas = new ArrayList<>();
            for (CamadaJson c : dados.camadas())
                camadas.add(new Camada(c.id(), c.nome(), c.visivel(), c.bloqueada()));
            for (Camada camada : camadas) documento.adicionarCamada(camada);
            documento.removerCamada(documento.getIdCamadaInicial());
            for (FormaJson f : dados.formas()) documento.adicionarForma(converter(f));
            documento.validarIntegridade();
            return documento;
        } catch (RuntimeException ex) {
            throw new IOException("Conteúdo JSON inválido: " + ex.getMessage(), ex);
        }
    }

    private FormaJson converter(FormaGeometrica f) {
        String tipo = f instanceof Circulo ? "circulo" : f instanceof Quadrado ? "quadrado"
                : f instanceof Hexagono ? "hexagono" : "poligono";
        List<PontoJson> vertices = f.getPontos().stream().map(p -> new PontoJson(p.getX(), p.getY())).toList();
        PontoJson centro = null, u = null, v = null;
        if (f instanceof Circulo c) {
            centro = ponto(c.getCentro()); u = ponto(c.getEixoU()); v = ponto(c.getEixoV());
        }
        return new FormaJson(tipo, f.getId(), f.getLayerId(), f.getGroupId(),
                f.getCorPreenchimentoHex(), f.getCorBordaHex(), f.getEspessuraBorda(), vertices, centro, u, v);
    }
    private FormaGeometrica converter(FormaJson f) {
        if (f == null || f.tipo() == null) throw new IllegalArgumentException("Forma sem tipo.");
        FormaGeometrica forma;
        if (f.tipo().equals("circulo")) {
            forma = new Circulo(f.id(), ponto(f.centro()), ponto(f.eixoU()), ponto(f.eixoV()));
        } else {
            if (f.pontos() == null || f.pontos().size() > 10_000)
                throw new IllegalArgumentException("Vértices inválidos.");
            List<Ponto> pontos = f.pontos().stream().map(this::ponto).toList();
            forma = switch (f.tipo()) {
                case "poligono" -> new Poligono(f.id(), pontos);
                case "quadrado" -> new Quadrado(f.id(), pontos);
                case "hexagono" -> new Hexagono(f.id(), pontos);
                default -> throw new IllegalArgumentException("Tipo geométrico desconhecido: " + f.tipo());
            };
        }
        forma.setLayerId(f.layerId());
        forma.setGroupId(f.groupId());
        forma.setCorPreenchimentoHex(f.preenchimento());
        forma.setCorBordaHex(f.borda());
        forma.setEspessuraBorda(f.espessura());
        return forma;
    }
    private PontoJson ponto(Ponto p) { return new PontoJson(p.getX(), p.getY()); }
    private Ponto ponto(PontoJson p) {
        if (p == null) throw new IllegalArgumentException("Coordenada ausente.");
        return new Ponto(p.x(), p.y());
    }

    private record DocumentoJson(int versao, double largura, double altura, int passoGrade,
                                 boolean snapAtivo, boolean mostrarGrade,
                                 List<CamadaJson> camadas, List<FormaJson> formas) { }
    private record CamadaJson(String id, String nome, boolean visivel, boolean bloqueada) { }
    private record PontoJson(double x, double y) { }
    private record FormaJson(String tipo, String id, String layerId, String groupId,
                             String preenchimento, String borda, double espessura,
                             List<PontoJson> pontos, PontoJson centro, PontoJson eixoU, PontoJson eixoV) { }
}
