# Vetor Studio — Editor de Desenho Vetorial Interativo

Projeto acadêmico da disciplina **Programação Orientada a Objetos 1**, Faculdade de Engenharia de Software — UniRV, desenvolvido seguindo a especificação da avaliação prática N3 (2026/1).

Aplicação desktop em **Java 17 + JavaFX 21**, com **Maven**, **MVC**, **State**, **Command**, **Memento**, **Prototype** e agrupamento lógico **Composite**. O objetivo é permitir criação, edição, transformações e armazenamento de figuras 2D.

## 1. Pré-requisitos

- **JDK 17 ou superior** (`java -version` e `javac -version`);
- **Apache Maven 3.8 ou superior** (`mvn -version`);
- Conexão com a internet para baixar dependências Maven na primeira execução;
- Ambiente gráfico de desktop (Windows, Linux com servidor gráfico ou macOS).

O Maven baixa o JavaFX e o Jackson automaticamente. Não é necessário instalar manualmente o SDK JavaFX ao executar pelo plugin.

## 2. Iniciar o programa

Abra um terminal **na pasta que contém o `pom.xml`**:

```bash
mvn clean javafx:run
```

Para executar os testes automatizados:

```bash
mvn clean test
```

Para compilar e empacotar:

```bash
mvn clean package
```

> Atenção: o JAR produzido pelo comando `package` não incorpora o JavaFX nem suas bibliotecas. Para iniciar o aplicativo, use `mvn javafx:run`. Uma distribuição autônoma exigiria empacotamento adicional (`jlink`/`jpackage`).

## 3. Como utilizar

1. **Selecionar**: clique em uma forma para selecioná-la; use **Ctrl+clique** (ou Shift+clique) para múltipla seleção. Ao clicar em uma forma agrupada, todos os membros editáveis desse grupo são selecionados.
2. **Polígono livre**: clique sucessivamente para adicionar **ao menos três vértices**; dê duplo clique para finalizar. Botão direito cancela o polígono em edição.
3. **Círculo, quadrado, hexágono**: escolha a ferramenta, pressione o mouse e arraste para determinar o tamanho. Polígono regular é calculado automaticamente.
4. **Mover**: escolha a ferramenta e arraste um objeto ou grupo. Cada arrasto gera **um** registro de histórico, em vez de registrar cada pixel.
5. **Vértices**: arraste um dos quadradinhos azuis de um polígono selecionado. Quadrados e hexágonos transformam-se em polígonos livres ao editar seus vértices, para não representar um polígono irregular como se ainda fosse regular.
6. **Aparência**: escolha cores e espessura da borda. Se houver seleção, essas propriedades são aplicadas às formas selecionadas.
7. **Grade**: habilite/desabilite a visualização, ajuste o espaçamento e ative/desative `snap to grid`.
8. **Transformações**: com objetos selecionados, use escala, rotação em graus e cisalhamento (`shx`, `shy`) no painel lateral.
9. **Camadas**: crie, renomeie, exclua, mude a ordem, bloqueie ou oculte camadas. A camada selecionada recebe as formas novas; formas de camadas bloqueadas não são selecionáveis pelo mouse.
10. **Agrupamento**: selecione pelo menos duas formas, clique em **Agrupar**; uma transformação move/rotaciona/escala todo o grupo em torno de um pivô comum. Clique em **Desagrupar** para separar.
11. **Salvar/Abrir**: os desenhos são armazenados em arquivos JSON. Arquivos ilegíveis geram alerta sem fechar o aplicativo. Alterações não salvas são verificadas antes de fechar, iniciar um documento ou abrir outro.

### Atalhos de teclado

| Tecla | Ação |
|---|---|
| Ctrl+N | Novo documento |
| Ctrl+O | Abrir JSON |
| Ctrl+S | Salvar JSON |
| Ctrl+Shift+S | Salvar como |
| Ctrl+Z | Desfazer |
| Ctrl+Y | Refazer |
| Ctrl+D | Duplicar seleção |
| Delete | Excluir seleção |
| Ctrl+G | Agrupar seleção |
| Ctrl+Shift+G | Desagrupar seleção |

No macOS, use `Command` onde os atalhos indicam `Ctrl`.

## 4. Estrutura do código-fonte

```text
EditorVetorial-UniRV/
├── pom.xml
├── README.md
├── diagrama_classes.pdf
├── exemplo_projeto.json
├── docs/
│   ├── uml_models.dot
│   ├── uml_controller.dot
│   └── uml_persistence.dot
├── .github/workflows/testes.yml
└── src/
    ├── main/
    │   ├── java/org/example/
    │   │   ├── Models/
    │   │   │   ├── FormaGeometrica.java
    │   │   │   ├── FormaBase.java
    │   │   │   ├── Ponto.java
    │   │   │   ├── Poligono.java
    │   │   │   ├── Circulo.java
    │   │   │   ├── Quadrado.java
    │   │   │   ├── Hexagono.java
    │   │   │   ├── Camada.java
    │   │   │   ├── DocumentoDesenho.java
    │   │   │   └── Transformacoes.java
    │   │   ├── Controller/
    │   │   │   ├── EditorContext.java
    │   │   │   ├── Ferramenta.java
    │   │   │   ├── Comando.java
    │   │   │   ├── EditorMemento.java
    │   │   │   ├── HistoricoController.java
    │   │   │   └── ferramentas/
    │   │   │       ├── FerramentaSelecionar.java
    │   │   │       ├── FerramentaDesenhar.java
    │   │   │       ├── FerramentaInserirForma.java
    │   │   │       ├── FerramentaTransformar.java
    │   │   │       └── FerramentaEditarVertice.java
    │   │   ├── Views/
    │   │   │   ├── ContainerApp.java
    │   │   │   ├── BarraLateralView.java
    │   │   │   └── CanvasRenderer.java
    │   │   └── Persistence/
    │   │       └── ArquivoProjeto.java
    │   └── resources/org/example/Views/styles.css
    └── test/java/org/example/
        ├── Models/GeometriaTest.java
        ├── Controller/HistoricoControllerTest.java
        └── Persistence/ArquivoProjetoTest.java
```

## 5. Arquitetura e padrões

### MVC

- **Models** contém entidades e matemática geométrica, sem dependência de JavaFX;
- **Controller** concentra os estados das ferramentas, histórico, seleção, comandos e regras de edição;
- **Views** cria e desenha a interface JavaFX, delegando as regras ao controlador;
- **Persistence** é um serviço separado de entrada e saída JSON.

### State

`EditorContext` armazena uma referência à interface `Ferramenta`. Cada implementação concreta recebe os eventos do mouse, evitando condicionais extensos no controle principal da UI.

### Command + Memento

`Comando` representa uma alteração do documento. Antes de executar um comando, `HistoricoController` captura um `EditorMemento` com clone profundo da situação atual. Duas pilhas limitadas a 100 estados permitem desfazer e refazer. Durante os arrastos, um snapshot é capturado no início e registrado apenas ao término da alteração.

### Prototype

A interface `FormaGeometrica` declara `clonar()`. Todos os tipos implementam uma cópia completa, preservando identidade e estilo sem compartilhar estruturas mutáveis.

### Composite lógico

`groupId` identifica as formas pertencentes a um mesmo grupo. `EditorContext.membrosDoGrupo(...)` implementa a seleção expandida; `Transformacoes` aplica mudanças sobre o conjunto ao redor de um **pivô comum**. Não é necessário introduzir uma classe de nó de árvore, pois o enunciado explicitamente exige identificação por `groupId`.

## 6. Geometria e validação

- **Polígono**: pelo menos 3 vértices; área pela fórmula de Gauss, perímetro pela distância euclidiana, centroide pela área assinada (fallback pela média quando degenerado) e seleção via ray casting, incluindo bordas.
- **Quadrado**: quatro vértices gerados a partir da posição inicial e lado positivo.
- **Hexágono**: seis vértices igualmente espaçados ao redor do centro por ângulos de 60°.
- **Círculo**: raio estritamente positivo. O modelo utiliza centro + dois vetores-base para que **cisalhamento e rotação não sejam perdidos**; depois do cisalhamento, pode ser exibido como elipse afim. Área calculada pelo determinante dos eixos; perímetro por integração numérica de Simpson.
- **Translação, escala e rotação**: aplicadas nos pontos ou vetores em torno do centroide e, nos grupos, do centro conjunto.
- **Estilo**: cores hexadecimais validadas (`#RRGGBB` ou `#RRGGBBAA`); espessura de borda limitada e números não finitos rejeitados.

**Observação sobre unidades:** as coordenadas são pixels do documento. Área = px²; perímetro = px.

## 7. Persistência e recuperação de erros

`ArquivoProjeto` persiste `versao`, largura/altura do canvas, tamanho/visibilidade da grade, snap, camadas e formas, incluindo identificadores, agrupamentos, estilos e coordenadas. A implementação **não desserializa tipos arbitrários** informados no arquivo: somente os tipos declarados são aceitos.

O salvamento é realizado em um arquivo temporário no mesmo diretório e movido para o destino (com substituição atômica quando suportada). O carregamento impõe limite de tamanho e valida IDs, tipos e integridade de camadas. `ContainerApp` converte falhas em diálogos JavaFX sem encerrar a aplicação.

O arquivo `exemplo_projeto.json` acompanha o projeto para demonstração da interface.

## 8. Testes automatizados

- Área, perímetro, centroide, hit-test e validações;
- Escala, rotação, cisalhamento e transformação de grupos;
- Prototype e independência de snapshots;
- Snap to Grid;
- Undo/Redo;
- Round-trip JSON preservando estilos, grupos e camadas;
- Tratamento de JSON corrompido.

Execute `mvn test`. O workflow `.github/workflows/testes.yml` executa esses testes em commits e pull requests usando Java 17.

## 9. Material de entrega

- Código Maven completo (`src/`, `pom.xml`);
- README com instruções e descrição arquitetural;
- Diagrama de classes (`diagrama_classes.pdf`), em três páginas, com fontes Graphviz em `docs/`;
- Arquivo de exemplo (`exemplo_projeto.json`);
- Testes JUnit 5.

**Repositório Git:** este arquivo ZIP é o código-fonte pronto para versionamento. Crie um repositório no GitHub/GitLab, envie a pasta do projeto e entregue **a URL real do repositório** no portal acadêmico. O ZIP não cria nem publica o repositório por conta própria.

### Observação sobre execução

O código dos modelos e do histórico pode ser compilado isoladamente com `javac` 17+. A verificação integral em ambiente gráfico depende das bibliotecas Maven (JavaFX/Jackson), que devem ser baixadas no ambiente do usuário. Os testes Maven foram escritos para execução, mas não se deve interpretar sua presença como comprovação de aprovação sem a execução efetiva.
