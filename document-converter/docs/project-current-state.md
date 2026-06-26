# Document Converter - Documentacao do Estado Atual

> Observacao: o status funcional mais atualizado por plataforma esta em `docs/features-status.md`.
> Este documento foi mantido como historico consolidado do projeto.

## Visao Geral

O Document Converter e um projeto de conversao de documentos com duas frentes no momento:

- Aplicacao desktop JavaFX funcional.
- Prova de conceito Android em Kotlin/Compose para validar compatibilidade antes do app Android final.

O projeto desktop foi refatorado para separar interface grafica e logica de conversao:

```text
document-converter/
├── core/
├── desktop/
├── android-poc/
├── docs/
├── scripts/
├── future-android/
└── pom.xml
```

## Modulos

### core

Modulo Maven com a logica principal de conversao e modelos compartilhaveis.

Local:

```text
core/
└── src/main/java/com/documentconverter/core/
```

Pacotes principais:

```text
converters/
models/
services/
utils/
```

Conversores atuais:

- `ImageToPdfConverter`
- `PdfToImageConverter`
- `DocxToPdfConverter`
- `PdfToDocxConverter`

Servicos/modelos atuais:

- `ConversionService`
- `PdfToImageService`
- `ConversionHistory`
- `ConversionHistoryService`
- `ConversionRequest`
- `ConversionResult`
- `DefaultConversionRequest`
- `DefaultConversionResult`
- `PdfToDocxResult`

Dependencias principais:

- Apache PDFBox 3.0.3
- Apache POI 5.3.0

Observacao importante:

O `core` nao depende de JavaFX, mas ainda nao e totalmente Android-ready porque contem partes com PDFBox desktop, Apache POI, AWT, ImageIO, `File` e LibreOffice Headless.

### desktop

Modulo Maven com a aplicacao JavaFX.

Local:

```text
desktop/
└── src/main/
    ├── java/com/documentconverter/desktop/javafx/
    └── resources/
```

Responsabilidades:

- Exibir menu principal.
- Carregar telas FXML.
- Selecionar arquivos e destinos.
- Executar conversoes em background com `Task`.
- Registrar historico de conversoes.
- Mostrar progresso/status ao usuario.

Telas atuais:

- `MainMenuView.fxml`
- `ImageToPdfView.fxml`
- `PdfToImageView.fxml`
- `DocxToPdfView.fxml`
- `PdfToDocxView.fxml`
- `HistoryView.fxml`
- `MainView.fxml` legado/apoio

Controllers atuais:

- `MainMenuController`
- `ImageToPdfController`
- `PdfToImageController`
- `DocxToPdfController`
- `PdfToDocxController`
- `HistoryController`
- `MainController` legado/apoio

### android-poc

Projeto Android separado, criado apenas como prova de conceito.

Local:

```text
android-poc/
├── settings.gradle.kts
├── build.gradle.kts
├── local.properties
└── app/
```

Tecnologias:

- Kotlin
- Jetpack Compose
- Android Gradle Plugin 9.2.0
- Gradle 9.5.1
- compileSdk 36
- targetSdk 36
- minSdk 26

POCs implementadas:

- Imagem para PDF com `BitmapFactory`, `Bitmap`, `PdfDocument`, `ContentResolver` e SAF.
- PDF para imagem com `PdfRenderer`, `Bitmap` e `Bitmap.compress`.
- Abertura minima de DOCX com Apache POI.
- PDF para DOCX documentado como experimental/desktop-only.

APK debug gerado:

```text
android-poc/app/build/outputs/apk/debug/app-debug.apk
```

Tamanho medido:

```text
21.195.038 bytes, aproximadamente 20,2 MiB
```

### future-android

Pasta placeholder antiga para planejamento Android.

Local:

```text
future-android/README.md
```

No momento, a implementacao Android real de POC esta em `android-poc/`.

## Funcionalidades Desktop

### Imagem para PDF

Fluxo:

1. Usuario abre `Imagem -> PDF`.
2. Seleciona uma ou varias imagens.
3. Tambem pode arrastar imagens para a area de drop.
4. Escolhe arquivo PDF de saida.
5. Conversao roda em background.
6. Progresso/status sao exibidos.
7. Historico e registrado.

Formatos aceitos:

- Imagens suportadas pela implementacao atual do Java desktop/ImageIO.

Tratamentos:

- Arquivos invalidos.
- Mistura de formatos.
- Arquivos duplicados.
- Cancelamento de tarefa.

Tecnologia principal:

- PDFBox para criar PDF.
- ImageIO/BufferedImage para leitura de imagens.

### PDF para Imagem

Fluxo:

1. Usuario abre `PDF -> Imagem`.
2. Seleciona um PDF.
3. Tambem pode arrastar PDF para a area de drop.
4. Escolhe formato de saida.
5. Escolhe DPI.
6. Escolhe pasta de destino.
7. Cada pagina vira uma imagem.
8. Historico e registrado.

Formatos de saida:

- JPG
- JPEG
- PNG

DPI suportados:

- 150
- 300
- 600

Tecnologia principal:

- PDFBox `PDFRenderer`.
- BufferedImage.
- ImageIO.

### DOCX para PDF

Fluxo:

1. Usuario abre `DOCX -> PDF`.
2. Seleciona arquivo `.docx`.
3. Escolhe arquivo `.pdf` de saida.
4. Conversao roda em background.
5. Historico e registrado.

Tecnologia principal:

- LibreOffice Headless via `soffice`.

Limitacao:

- Requer LibreOffice instalado no ambiente desktop.
- Nao funciona no Android.
- Preservacao de formatacao depende do LibreOffice.

### PDF para DOCX

Fluxo:

1. Usuario abre `PDF -> DOCX`.
2. Seleciona arquivo `.pdf`.
3. Escolhe arquivo `.docx` de saida.
4. Conversao extrai texto e tenta preservar estrutura simples.
5. Historico e registrado.

Tecnologias:

- PDFBox para leitura/extracao.
- Apache POI para gerar DOCX.

Limitacoes:

- Layout complexo pode ser perdido.
- Tabelas complexas podem nao ser reconstruidas perfeitamente.
- No desktop/core, PDFs digitalizados sem texto pesquisavel ainda exigem OCR externo.
- No Android, PDF para DOCX usa OCR experimental com ML Kit quando a pagina nao possui texto selecionavel.
- PDF protegido por senha pode falhar.

### Historico

O historico registra:

- Data.
- Hora.
- Arquivo origem.
- Arquivo destino.
- Tipo de conversao.
- Status.

Persistencia:

```text
history/conversion-history.json
```

No desktop atual, se a aplicacao for executada a partir de `desktop/`, o arquivo fica em:

```text
desktop/history/conversion-history.json
```

Tela de historico:

- Mostra ultimas conversoes.
- Pesquisa por nome.
- Filtro por tipo.

## Drag and Drop

As telas de conversao possuem area central de drop:

```text
Arraste seus arquivos aqui
ou clique para selecionar
```

Comportamento:

- Imagem para PDF aceita uma ou varias imagens.
- PDF para Imagem aceita um PDF.
- Arquivos invalidos sao recusados.
- Misturas de formatos sao tratadas.
- Duplicados sao evitados quando aplicavel.

## Comandos Desktop

### Compilar e testar

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter"
.\mvnw.cmd test
```

Resultado atual validado:

```text
BUILD SUCCESS
```

### Rodar aplicacao desktop

Primeira vez ou apos alteracoes no `core`:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter"
.\mvnw.cmd install -DskipTests
.\mvnw.cmd -pl desktop javafx:run
```

Quando o `core` ja esta instalado/atualizado:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter"
.\mvnw.cmd -pl desktop javafx:run
```

Observacao:

O comando fica em execucao enquanto a janela JavaFX estiver aberta.

## Distribuicao Windows

Scripts:

```text
scripts/build.ps1
scripts/package-windows.ps1
```

Documentacao:

```text
docs/windows-distribution.md
```

Objetivo:

- Gerar estrutura de distribuicao.
- Empacotar com `jpackage`.
- Gerar instalador Windows `.exe`.
- Incluir runtime Java.
- Criar atalho.
- Usar icone personalizado.

Comandos principais:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter"
.\scripts\build.ps1
.\scripts\package-windows.ps1
```

Limitacao conhecida:

- Para gerar instalador `.exe`, o Windows precisa do WiX Toolset instalado.

## Ambiente Android Local

Ferramentas instaladas/configuradas:

```text
C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1
C:\Projetinhos\Projeto conversor pdf\.tools\android-sdk
```

Variaveis configuradas no usuario:

```text
ANDROID_HOME=C:\Projetinhos\Projeto conversor pdf\.tools\android-sdk
ANDROID_SDK_ROOT=C:\Projetinhos\Projeto conversor pdf\.tools\android-sdk
```

PATH do usuario inclui:

```text
C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1\bin
C:\Projetinhos\Projeto conversor pdf\.tools\android-sdk\cmdline-tools\latest\bin
C:\Projetinhos\Projeto conversor pdf\.tools\android-sdk\platform-tools
```

Pacotes Android SDK instalados:

```text
platform-tools
platforms;android-36
build-tools;36.1.0
```

Observacao:

E necessario abrir um novo terminal para variaveis/PATH do usuario serem recarregados.

## Comandos Android POC

### Build debug

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android-poc"
gradle :app:assembleDebug
```

Resultado atual validado:

```text
BUILD SUCCESSFUL
```

APK:

```text
android-poc/app/build/outputs/apk/debug/app-debug.apk
```

### Verificar ADB

```powershell
adb version
```

Resultado atual validado:

```text
Android Debug Bridge version 1.0.41
Version 37.0.0-14910828
```

## Dependencias Principais

### Desktop / Core

- Java 21.
- Maven Wrapper 3.9.9.
- JavaFX 21.0.6.
- PDFBox 3.0.3.
- Apache POI 5.3.0.
- LibreOffice Headless para DOCX -> PDF.

### Android POC

- Gradle 9.5.1.
- Android Gradle Plugin 9.2.0.
- Kotlin/Compose plugin 2.2.21.
- Compose BOM 2026.05.00.
- Activity Compose 1.12.0.
- Material 3.
- Apache POI 5.3.0.

## Arquivos de Documentacao

```text
docs/project-current-state.md
docs/windows-distribution.md
docs/android-readiness-audit.md
docs/android-poc-results.md
```

## Limitacoes Conhecidas

### Desktop

- DOCX para PDF depende do LibreOffice instalado.
- PDF para DOCX nao preserva layout complexo perfeitamente.
- PDF para DOCX no desktop ainda nao possui OCR embutido.
- PDF para DOCX no Android possui OCR experimental para texto latino usando ML Kit.
- Historico usa JSON local simples, nao banco de dados.
- O core possui testes unitarios basicos para conversoes principais; ainda faltam testes amplos com arquivos reais.

### Android POC

- Ainda nao e app Android final.
- Imagem para PDF gera uma pagina por imagem selecionada na POC atual.
- PDF para imagem renderiza apenas a primeira pagina.
- DOCX com POI apenas abre e mede informacoes basicas; nao converte para PDF.
- PDF para DOCX segue experimental/desktop-only.
- O APK debug atual e grande para uma POC, em parte por causa de Compose e Apache POI.

## Recomendacoes de Proximos Passos

1. Criar testes unitarios no `core`.
2. Separar `core-api`, `core-jvm` e futuros adaptadores Android.
3. Criar abstracoes de entrada/saida para substituir uso direto de `File`.
4. Medir APK Android com e sem Apache POI.
5. Testar POC Android em aparelho/emulador real.
6. Validar conversoes com arquivos reais:
   - imagem grande;
   - PDF pequeno;
   - PDF protegido;
   - DOCX simples;
   - DOCX com tabela/imagem.
7. Instalar WiX Toolset para validar instalador Windows completo.

## Estado Atual Resumido

- Desktop JavaFX: compila e executa.
- Core Maven: compila.
- Historico: implementado em JSON local.
- Drag and drop: implementado nas telas principais.
- Distribuicao Windows: scripts/documentacao criados; instalador depende de WiX.
- Android POC: compila e gera APK debug.
- App Android final: ainda nao criado.
