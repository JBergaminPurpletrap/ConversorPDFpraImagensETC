# FASE 12 - Preparacao Android

## Objetivo

Auditar o projeto Document Converter para identificar o que pode ser reutilizado no Android, o que precisa ser reimplementado e qual estrategia deve ser usada para chegar a estrutura:

```text
document-converter/
├── core/
├── desktop/
└── android/
```

Esta auditoria foi feita sobre a arquitetura atual, ja refatorada em modulos Maven:

```text
document-converter/
├── core/
├── desktop/
├── future-android/
├── docs/
└── scripts/
```

## Estado Atual

### Modulo core

O modulo `core` contem:

- `com.documentconverter.core.converters`
- `com.documentconverter.core.services`
- `com.documentconverter.core.models`
- `com.documentconverter.core.utils`

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

### Modulo desktop

O modulo `desktop` contem:

- Aplicacao JavaFX.
- Controllers.
- FXML.
- CSS.
- Icones.
- Dependencia no modulo `core`.

### Modulo Android

Ainda nao existe modulo Android real. Existe apenas:

```text
future-android/README.md
```

## Dependencias Compatíveis

### Java padrao parcialmente reutilizavel

As seguintes familias sao conceitualmente reutilizaveis, mas precisam ser avaliadas no contexto do Android Gradle Plugin e da versao minSdk:

- `java.io.File`
- `java.io.IOException`
- `java.util.List`
- `java.util.Map`
- `java.util.Locale`
- `java.util.ArrayList`
- `java.util.Collections`
- `java.util.function.BiConsumer`
- `java.time.*`
- `java.util.regex.*`

Observacao: `java.io.File` funciona em Android, mas o acesso a arquivos deve respeitar Storage Access Framework, `ContentResolver`, URIs e permissoes. Portanto, o modelo atual baseado apenas em `File` nao e ideal para Android.

### Modelos do core

Reaproveitaveis com poucos ajustes:

- `ConversionHistory`
- `ConversionRequest`
- `ConversionResult`
- `DefaultConversionRequest`
- `DefaultConversionResult`
- `PdfToDocxResult`

Risco principal: `ConversionRequest` e `ConversionResult` usam `java.io.File`. Para Android, o ideal e introduzir abstracoes de entrada/saida que possam representar `File`, `Uri` ou streams.

## Dependencias Incompatíveis ou Problematicas

## Matriz de Compatibilidade das Bibliotecas

| Biblioteca / Tecnologia | Compatibilidade Android | Veredito | Observacao |
| --- | --- | --- | --- |
| JavaFX / OpenJFX | Nao compativel para app Android nativo | Nao reutilizar | Deve permanecer apenas no modulo `desktop`. Android precisa UI propria. |
| Kotlin UI / Jetpack Compose | Compativel | Recomendado | Melhor caminho para UI Android moderna. |
| Android Views XML | Compativel | Alternativa valida | Boa opcao se o app Android preferir arquitetura tradicional. |
| Apache PDFBox JVM | Parcial / problematico | Exige adaptacoes | Pode servir para partes de texto em JVM, mas renderizacao usa APIs problematicas no Android. Avaliar alternativa Android-specific. |
| PDFBox-Android | Mais adequado que PDFBox JVM | Avaliar POC | Alternativa especifica para Android, mas precisa prova de conceito para cada conversao. |
| Apache POI | Parcial / pesado | Usar com cautela | Pode funcionar em alguns cenarios, mas aumenta APK e pode trazer dependencias complexas. |
| LibreOffice Headless | Incompativel | Nao reutilizar | Depende de `soffice.exe`/processo desktop. Nao existe equivalente direto no Android. |
| PDFBox + AWT / BufferedImage | Incompativel | Reimplementar | Android usa `Bitmap`, nao `BufferedImage`. |
| javax.imageio.ImageIO | Incompativel | Reimplementar | Android usa `BitmapFactory`, `Bitmap.compress` e APIs de `android.graphics`. |
| jpackage / WiX | Incompativel | Desktop-only | Serve apenas para distribuicao Windows. |
| Maven desktop | Nao e padrao Android | Separar | Android deve usar Gradle/Android Gradle Plugin. |
| java.io.File | Parcial | Adaptar | Android moderno deve usar `Uri`, SAF e `ContentResolver` para arquivos do usuario. |
| java.nio.file.Files | Parcial / restrito | Evitar no core Android | Melhor abstrair persistencia e usar APIs Android. |

### JavaFX

Localizacao: `desktop`.

Status: incompativel com Android.

Impacto:

- Correto estar restrito ao modulo `desktop`.
- Nao ha referencia a JavaFX dentro do `core`.

Conclusao:

- Nao migrar JavaFX.
- Android deve ter UI propria com Kotlin/Java + Android SDK, provavelmente Compose ou Views.

### Apache PDFBox

Localizacao: `core/pom.xml`.

Uso atual:

- `ImageToPdfConverter`
- `PdfToImageConverter`
- `PdfToDocxConverter`

Riscos no Android:

- PDFBox oficial e voltado para JVM desktop/server.
- Pode depender de classes e comportamentos nao ideais no Android.
- Renderizacao de PDF usa `java.awt.image.BufferedImage` e `PDFRenderer`, ambos problematicos no Android.
- Para Android existe alternativa historica como `PdfBox-Android`, mas nao e substituicao perfeita.

Conclusao:

- Parte de leitura textual pode ser migravel com biblioteca Android adequada.
- Renderizacao de paginas e manipulacao de imagens devem ser reavaliadas.

### Apache POI

Localizacao: `core/pom.xml`.

Uso atual:

- `PdfToDocxConverter`, via `XWPFDocument`.

Riscos no Android:

- POI e pesado.
- Pode aumentar muito o APK.
- Pode exigir dependencias problematicas.
- Pode gerar problemas de metodo/DEX em projetos Android.

Conclusao:

- PDF para DOCX no Android nao deve depender automaticamente do mesmo POI usado no desktop sem prova tecnica.
- Recomendado isolar em conversor especifico de desktop ou criar adaptador Android separado.

### AWT e ImageIO

Localizacao no core:

- `java.awt.image.BufferedImage`
- `javax.imageio.ImageIO`

Arquivos:

- `ImageToPdfConverter`
- `PdfToImageConverter`
- `PdfToDocxConverter`

Status: incompativel com Android.

Impacto:

- Android usa `android.graphics.Bitmap`, nao `BufferedImage`.
- Android nao possui `javax.imageio.ImageIO` da JVM desktop.

Conclusao:

- Conversoes com imagem precisam de uma camada de abstracao.
- Implementacao Android deve usar `Bitmap`, `BitmapFactory`, `Canvas`, `PdfRenderer` ou bibliotecas Android-native.

### LibreOffice Headless

Localizacao:

- `DocxToPdfConverter`

Uso atual:

- `ProcessBuilder`
- `System.getenv("LIBREOFFICE_HOME")`
- caminhos Windows como `C:/Program Files/LibreOffice/program/soffice.exe`
- execucao de `soffice.exe`

Status: incompativel com Android.

Impacto:

- Android nao executa LibreOffice desktop.
- Nao ha `soffice.exe`.
- `ProcessBuilder` nao resolve esse caso.

Conclusao:

- DOCX para PDF deve ser reimplementado para Android ou delegado a backend/servico externo.
- A implementacao atual deve ser considerada desktop-only.

### Persistencia de historico por arquivo local

Localizacao:

- `ConversionHistoryService`

Uso atual:

- pasta relativa `history/`
- arquivo `history/conversion-history.json`
- `Files.readString`
- `Files.writeString`

Riscos no Android:

- Caminho relativo nao e adequado.
- Android deve usar `Context.getFilesDir()`, Room, DataStore ou outro mecanismo.
- A classe atual nao recebe uma abstracao de armazenamento.

Conclusao:

- Modelo `ConversionHistory` e reaproveitavel.
- `ConversionHistoryService` deve ser dividido em interface + implementacao desktop + implementacao Android.

### java.util.logging

Localizacao:

- varios arquivos do core.

Status:

- Pode funcionar parcialmente, mas nao e ideal para Android.

Conclusao:

- Criar abstracao de logging ou usar eventos/retornos estruturados.
- Android deve usar Logcat (`android.util.Log`) ou logger compativel.

## Auditoria por Conversor

### ImageToPdfConverter

Reaproveitavel no Android: parcialmente.

Problemas:

- Usa `BufferedImage`.
- Usa `ImageIO.read`.
- Usa PDFBox para criar PDF.
- Recebe `File`.

Estrategia:

- Manter versao desktop/JVM.
- Criar interface comum para imagem.
- Implementacao Android deve ler imagens via `Uri`/`Bitmap`.
- Avaliar geracao de PDF via `android.graphics.pdf.PdfDocument` ou biblioteca Android.

### PdfToImageConverter

Reaproveitavel no Android: baixo.

Problemas:

- Usa `PDFRenderer`.
- Usa `BufferedImage`.
- Usa `ImageIO.write`.

Estrategia:

- No Android, usar `android.graphics.pdf.PdfRenderer` para renderizar paginas.
- Exportar imagem via `Bitmap.compress`.
- Criar implementacao Android separada.

### DocxToPdfConverter

Reaproveitavel no Android: nao.

Problemas:

- Depende de LibreOffice Headless.
- Depende de processo externo desktop.
- Procura `soffice.exe`.

Estrategia:

- Desktop continua usando LibreOffice.
- Android deve usar outro caminho:
  - conversao via backend;
  - servico cloud;
  - biblioteca Android especializada;
  - ou limitar suporte inicial.

### PdfToDocxConverter

Reaproveitavel no Android: baixo a medio, dependendo da biblioteca.

Problemas:

- Usa PDFBox.
- Usa POI.
- Usa `BufferedImage`/`ImageIO` para imagens.
- Usa `FileOutputStream`.

Estrategia:

- Separar extracao de texto de extracao de imagens.
- Avaliar biblioteca PDF Android-compatible.
- Avaliar geracao DOCX no Android com POI apenas apos prova de conceito.
- Alternativa: gerar TXT/HTML no Android e deixar DOCX para desktop/backend.

## O Que Pode Ser Reaproveitado

Reaproveitamento direto:

- Modelos simples sem UI:
  - `ConversionHistory`
  - `PdfToDocxResult`
- Contratos:
  - `Converter`
  - `ConversionRequest`
  - `ConversionResult`
- Regras de alto nivel:
  - nomes de operacoes;
  - status de historico;
  - validacoes conceituais de extensao/formato.

Reaproveitamento com ajustes:

- `ConversionService`
- `ConversionHistoryService`
- validacoes de entrada/saida
- formatacao de mensagens/logs

Reaproveitamento improvavel sem alteracoes:

- `ImageToPdfConverter`
- `PdfToImageConverter`
- `PdfToDocxConverter`

Nao reaproveitavel no Android:

- `DocxToPdfConverter` baseado em LibreOffice Headless.
- Qualquer codigo JavaFX.
- Scripts Windows/jpackage.

## O Que Deve Ser Reimplementado

### No Android

Reimplementar:

- Selecao de arquivos via Storage Access Framework.
- Escrita de arquivos via `ContentResolver`/URI.
- Historico usando DataStore, Room ou arquivo interno Android.
- Renderizacao PDF para imagem com APIs Android.
- Imagem para PDF usando `PdfDocument` ou biblioteca nativa Android.
- DOCX para PDF por estrategia separada.
- PDF para DOCX com prova de conceito especifica.

### No Core

Refatorar antes de reuso real:

- Trocar contratos baseados em `File` por abstracoes:
  - `ConversionInput`
  - `ConversionOutput`
  - `StorageProvider`
  - `HistoryRepository`
- Separar conversores JVM desktop de conversores portaveis.
- Criar interfaces por operacao, por exemplo:
  - `ImageToPdfUseCase`
  - `PdfToImageUseCase`
  - `DocxToPdfUseCase`
  - `PdfToDocxUseCase`

## Estrategia de Migracao

### Fase A - Separar core-portable e core-jvm

Estrutura sugerida:

```text
document-converter/
├── core/
│   ├── core-api/
│   ├── core-jvm/
│   └── core-android/
├── desktop/
└── android/
```

Opcao mais simples, mantendo a estrutura pedida:

```text
document-converter/
├── core/
│   ├── api/
│   ├── jvm/
│   └── android-adapters/
├── desktop/
└── android/
```

### Fase B - Criar abstracoes de armazenamento

Problema atual:

```java
File source;
File destination;
```

Proposta:

```java
interface ConversionInput {
    String displayName();
    InputStream openStream();
}

interface ConversionOutput {
    String displayName();
    OutputStream openStream();
}
```

No desktop:

- `FileConversionInput`
- `FileConversionOutput`

No Android:

- `UriConversionInput`
- `UriConversionOutput`

### Fase C - Separar persistencia de historico

Proposta:

```java
interface HistoryRepository {
    List<ConversionHistory> findAll();
    void save(ConversionHistory history);
}
```

Desktop:

- JSON local em `history/conversion-history.json`.

Android:

- Room, DataStore ou arquivo interno do app.

### Fase D - Implementar android/

Estrutura Android alvo:

```text
android/
├── app/
│   ├── build.gradle
│   └── src/main/
└── README.md
```

O modulo Android deve depender apenas de APIs portaveis do core, nao de JavaFX, jpackage ou scripts Windows.

### Fase E - Provas de conceito por conversao

Prioridade recomendada:

1. Imagem para PDF.
2. PDF para imagem.
3. Historico Android.
4. PDF para DOCX simples.
5. DOCX para PDF, provavelmente via backend ou biblioteca externa.

## Estrutura Final Recomendada

Estrutura pedida:

```text
document-converter/
├── core/
├── desktop/
└── android/
```

Estrutura interna recomendada:

```text
core/
├── src/main/java/com/documentconverter/core/api/
├── src/main/java/com/documentconverter/core/models/
├── src/main/java/com/documentconverter/core/services/
├── src/main/java/com/documentconverter/core/ports/
└── src/main/java/com/documentconverter/core/jvm/

desktop/
├── src/main/java/com/documentconverter/desktop/javafx/
└── src/main/resources/

android/
├── app/
└── README.md
```

## Conclusao

O projeto esta melhor preparado para Android do que antes da Fase 11 porque:

- JavaFX esta isolado no modulo `desktop`.
- A UI depende do `core`, nao o contrario.
- Existem contratos iniciais para conversao.

Mas o `core` atual ainda nao e Android-ready porque contem:

- PDFBox desktop/JVM.
- Apache POI.
- AWT/BufferedImage.
- ImageIO.
- LibreOffice Headless via `ProcessBuilder`.
- Persistencia baseada em arquivo relativo.

Portanto, a proxima alteracao recomendada nao e criar imediatamente o app Android, mas separar o core em API portavel e implementacoes especificas por plataforma.
