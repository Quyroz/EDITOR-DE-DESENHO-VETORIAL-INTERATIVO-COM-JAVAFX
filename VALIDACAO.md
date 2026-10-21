# Relatório de validação

## Verificações realizadas neste ambiente

1. **Java 17 (JDK 21 compilando com `--release 17`)** — compilação de `Models`, `Comando`, `EditorMemento` e `HistoricoController`: **aprovada**.
2. **Execução das regras geométricas e histórico** — **14 testes automatizados passaram**, incluindo área, perímetro, centroide, validação de entrada, círculo, quadrado, hexágono, transformação, grupo, Prototype, Snap, Memento e Undo/Redo. Os métodos foram executados por um runner local que replica as assertivas usadas nos testes JUnit, pois o JUnit não está instalado no ambiente.
3. **Checagem de tipos e sintaxe do projeto Java completo** — todas as fontes de `src/main/java` e `src/test/java` foram compiladas com APIs *substitutas locais* do JavaFX, Jackson e JUnit. Isso verifica a estrutura de código, **mas não substitui uma compilação contra as bibliotecas reais**.
4. **UML** — PDF de três páginas criado e inspecionado após renderização.
5. **Empacotamento** — arquivos do projeto incluídos no ZIP sem dependências binárias ou artefatos temporários.

## O que não foi possível verificar neste ambiente

- Não há Apache Maven instalado nem resolução de rede para baixar as dependências JavaFX, Jackson e JUnit.
- Consequentemente, não foi possível iniciar a GUI real com `mvn javafx:run` nem executar o `mvn test` original, especialmente o teste de persistência JSON.

**Antes da entrega definitiva:** no computador com Maven e ambiente gráfico, executar:

```bash
mvn clean verify
mvn javafx:run
```

Na interface, abrir `exemplo_projeto.json`, verificar o desenho e testar salvar, reabrir, selecionar, arrastar, transformar, agrupar, desfazer e refazer.

O código foi estruturado para atender ao enunciado, mas nenhuma alegação de execução integral é feita sem o teste real no ambiente final.
