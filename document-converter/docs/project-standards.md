# Padronizacao do Projeto Document Converter

## Objetivo

Este documento define padroes para manter o projeto consistente, previsivel e facil de evoluir.

Escopo:

- Frontend Android: layout, cores, componentes e telas.
- Frontend Desktop: FXML, CSS, cores e componentes JavaFX.
- Backend/Core: classes, pastas, responsabilidades e nomenclatura.
- Documentacao e build.

## Frontend Android

### Estrutura de pastas

```text
android/app/src/main/java/com/documentconverter/android/
├── converter/
├── ui/
│   ├── components/
│   ├── model/
│   ├── navigation/
│   ├── screens/
│   │   ├── about/
│   │   ├── converter/
│   │   ├── home/
│   │   └── result/
│   ├── state/
│   └── theme/
├── util/
├── viewmodel/
└── MainActivity.kt
```

### Responsabilidades

- `ui/screens`: telas Compose completas, separadas por dominio.
- `ui/components`: componentes reutilizaveis, como cards, listas, status e conclusao.
- `ui/theme`: tema Material 3, cores, tipografia, dimensoes e shapes.
- `ui/navigation`: enum de telas e composable de navegacao visual.
- `ui/state`: estados visuais reaproveitaveis.
- `ui/model`: modelos usados pela interface.
- `viewmodel`: estado da tela e orquestracao das conversoes.
- `converter`: conversao real usando APIs Android.
- `util`: helpers Android, como compartilhamento, logs e nomes de documentos.

### Tema e cores

Arquivos principais:

```text
android/app/src/main/java/com/documentconverter/android/ui/theme/Color.kt
android/app/src/main/java/com/documentconverter/android/ui/theme/Dimens.kt
android/app/src/main/java/com/documentconverter/android/ui/theme/Shape.kt
android/app/src/main/java/com/documentconverter/android/ui/theme/Theme.kt
android/app/src/main/java/com/documentconverter/android/ui/theme/Type.kt
```

Padrao atual:

- Fundo principal: verde escuro.
- Cards/superficies: verde escuro levemente mais claro.
- Botoes: tons claros de verde/salvia.
- Texto dos botoes: preto.
- Texto comum sobre fundo escuro: claro.
- Erros: tom claro e pouco agressivo.

Regras:

- Nao usar cores diretamente nas telas quando ja existir cor em `AppColors`.
- Preferir `MaterialTheme.colorScheme` para superficies e textos padrao.
- Todos os botoes devem ter texto preto.
- Todo `Text` dentro de `ElevatedButton` ou `OutlinedButton` deve definir `color = AppColors.ButtonText` explicitamente.
- Evitar vermelho forte, roxo vibrante e azul saturado.
- Manter contraste legivel em uso prolongado.

### Botoes

Padrao:

- Usar `AppPrimaryButton` para acoes principais.
- Usar `AppSecondaryButton` para acoes secundarias.
- Usar `AppDimens` para altura minima.
- Texto deve ser curto e direto.

Exemplos de acoes principais:

- `Selecionar PDF`
- `Salvar JPEG`
- `Salvar PDF`
- `Compartilhar`

Exemplos de acoes secundarias:

- `Voltar`
- `PNG (.png)` quando nao selecionado.

### Telas

Cada tela deve:

- Usar `ScreenBody`.
- Exibir uma descricao curta no topo.
- Mostrar feedback de progresso com `StatusPanel`.
- Mostrar sucesso com `CompletionPanel` quando houver arquivo gerado.
- Usar `Snackbar` para sucesso e erro.

### Navegacao

O app usa navegacao simples por `AppScreen` em `ui/navigation/AppNavigation.kt`.

Regras:

- Botao de voltar do Android retorna para a tela anterior.
- Na Home, o usuario precisa pressionar voltar duas vezes para sair.
- Novas telas devem ser adicionadas ao enum `AppScreen`.

## Frontend Desktop

### Estrutura

```text
desktop/src/main/java/com/documentconverter/desktop/
├── javafx/
│   └── App.java
└── ui/
    ├── components/
    ├── model/
    ├── navigation/
    ├── screens/
    │   ├── about/
    │   ├── converter/
    │   ├── history/
    │   ├── home/
    │   ├── result/
    │   └── settings/
    ├── state/
    └── theme/

desktop/src/main/resources/
├── fxml/
├── icon.ico
├── icon.png
└── styles.css
```

### Responsabilidades

- `ui/screens`: controllers JavaFX agrupados por tipo de tela.
- `ui/components`: componentes JavaFX reutilizaveis para novas telas.
- `ui/theme`: constantes visuais, icones, espacamentos e aplicacao de CSS.
- `ui/navigation`: enum de telas e troca centralizada de cenas.
- `ui/state`: estados visuais reaproveitaveis.
- `ui/model`: modelos exclusivos da interface desktop.
- `javafx/App.java`: bootstrap da aplicacao.

### Tema visual

O desktop deve seguir a mesma identidade visual do Android:

- Fundo principal verde escuro: `#123528`.
- Superficies e campos: verdes escuros intermediarios.
- Botoes principais e de menu: verdes claros.
- Texto dos botoes: preto.
- Texto em fundo escuro: claro.
- Erros/cancelamento: tons suaves, sem vermelho agressivo.

### Regras de CSS

- Centralizar estilos em `desktop/src/main/resources/styles.css`.
- Evitar `style="-fx-..."` nos FXML.
- Usar classes como `title-label`, `subtitle-label`, `section-label`, `status-label`, `menu-button`, `drop-zone` e `separator-line`.
- Novos controles devem reaproveitar classes existentes antes de criar novas.
- Botoes claros sempre devem manter texto preto.
- Listas, tabelas, combos e text fields devem respeitar a paleta escura.

### Regras de FXML

- FXML deve declarar estrutura e bindings, nao identidade visual.
- Controllers nao devem conter cores, tamanhos visuais ou CSS inline.
- Separadores visuais devem usar `styleClass="separator-line"`.
- Telas de conversao devem manter cabecalho, subtitulo, area de selecao, progresso, status e rodape consistentes.

## Backend/Core Desktop

### Estrutura de pastas

```text
core/src/main/java/com/documentconverter/core/
├── converters/
├── models/
└── services/
```

### Responsabilidades

- `converters`: logica de conversao independente da UI.
- `services`: validacao, orquestracao e criacao de tarefas.
- `models`: contratos, requests, resultados e historico.

### Conversores

Padrao de classe:

```text
NomeDaOrigemToNomeDoDestinoConverter
```

Exemplos:

- `ImageToPdfConverter`
- `PdfToImageConverter`
- `DocxToPdfConverter`
- `PdfToDocxConverter`

Regras:

- Conversor nao deve depender de JavaFX.
- Conversor deve validar entradas criticas.
- Erros devem ser propagados com mensagens claras.
- Logica compartilhada deve ir para `core`, nao para controllers.

### Services

Services devem:

- Validar arquivos de entrada e saida.
- Criar tarefas/fluxos de conversao.
- Registrar historico quando aplicavel.
- Manter controllers finos.

### Models

Models devem:

- Representar dados simples.
- Evitar dependencia de UI.
- Usar nomes claros e consistentes.

## Controllers Desktop

Controllers JavaFX devem:

- Receber eventos de UI.
- Atualizar campos e labels.
- Delegar conversao para `core/services`.
- Nao conter logica pesada de conversao.

## Documentacao

Documentos ficam em:

```text
docs/
```

Padrao:

- Um documento por tema.
- Nomes em kebab-case.
- Incluir comandos executaveis quando for documentacao de build/release.

## Build e artefatos

Nao versionar:

- `build/`
- `target/`
- `.gradle/`
- APKs
- AABs
- JARs gerados
- `local.properties`
- Keystores
- Senhas de assinatura

Esses itens devem permanecer ignorados pelo `.gitignore`.

## Assinatura Android

Segredos de release ficam fora do projeto:

```text
C:\Projetinhos\Projeto conversor pdf\.local\document-converter-android\
```

Tambem podem ser configurados por variaveis de ambiente:

- `DOCUMENT_CONVERTER_STORE_FILE`
- `DOCUMENT_CONVERTER_STORE_PASSWORD`
- `DOCUMENT_CONVERTER_KEY_ALIAS`
- `DOCUMENT_CONVERTER_KEY_PASSWORD`

## Checklist antes de finalizar uma etapa

- Build Android debug passa.
- Quando houver release, APK/AAB release passam.
- Build Maven passa para core/desktop quando alterados.
- Documentacao atualizada.
- Nenhum segredo novo dentro do projeto.
- Nenhum artefato gerado tratado como codigo-fonte.
