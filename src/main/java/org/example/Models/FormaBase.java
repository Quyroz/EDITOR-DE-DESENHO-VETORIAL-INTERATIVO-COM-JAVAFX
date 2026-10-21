package org.example.Models;

import java.util.UUID;
import java.util.regex.Pattern;

/** Atributos de estilo e identidade encapsulados; o Prototype preserva o ID no snapshot. */
public abstract class FormaBase implements FormaGeometrica {
    private static final Pattern COR_HEX = Pattern.compile("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?");
    private final String id;
    private String corPreenchimentoHex = "#A5B4FC";
    private String corBordaHex = "#334155";
    private double espessuraBorda = 2.0;
    private String groupId;
    private String layerId;

    protected FormaBase() { this(UUID.randomUUID().toString()); }
    protected FormaBase(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID inválido.");
        this.id = id;
    }

    @Override public String getId() { return id; }
    @Override public String getCorPreenchimentoHex() { return corPreenchimentoHex; }
    @Override public String getCorBordaHex() { return corBordaHex; }
    @Override public double getEspessuraBorda() { return espessuraBorda; }
    @Override public String getGroupId() { return groupId; }
    @Override public String getLayerId() { return layerId; }

    public static void validarCorHex(String hex) {
        if (hex == null || !COR_HEX.matcher(hex).matches())
            throw new IllegalArgumentException("Cor inválida (use #RRGGBB ou #RRGGBBAA).");
    }
    public static void validarEspessura(double espessura) {
        Ponto.validarNumero(espessura, "Espessura");
        if (espessura < 0 || espessura > 100)
            throw new IllegalArgumentException("Espessura deve estar entre 0 e 100.");
    }
    @Override public void setCorPreenchimentoHex(String hex) {
        validarCorHex(hex);
        corPreenchimentoHex = hex.toUpperCase();
    }
    @Override public void setCorBordaHex(String hex) {
        validarCorHex(hex);
        corBordaHex = hex.toUpperCase();
    }
    @Override public void setEspessuraBorda(double espessura) {
        validarEspessura(espessura);
        espessuraBorda = espessura;
    }
    @Override public void setGroupId(String groupId) { this.groupId = groupId == null || groupId.isBlank() ? null : groupId; }
    @Override public void setLayerId(String layerId) {
        if (layerId == null || layerId.isBlank()) throw new IllegalArgumentException("Camada obrigatória.");
        this.layerId = layerId;
    }

    protected final void copiarAtributosPara(FormaBase destino) {
        destino.setCorPreenchimentoHex(corPreenchimentoHex);
        destino.setCorBordaHex(corBordaHex);
        destino.setEspessuraBorda(espessuraBorda);
        destino.setGroupId(groupId);
        if (layerId != null) destino.setLayerId(layerId);
    }
}
