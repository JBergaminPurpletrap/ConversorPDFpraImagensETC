# FASE 12.1 - Android POC de Compatibilidade

## Objetivo

Criar uma prova de conceito Android minima, em Kotlin e Jetpack Compose, para validar quais conversoes do Document Converter podem ser implementadas no Android sem reutilizar JavaFX, AWT, ImageIO, `File` direto ou LibreOffice.

## Projeto Criado

Local:

```text
android-poc/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── README.md
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/documentconverter/androidpoc/MainActivity.kt
        └── java/com/documentconverter/androidpoc/poc/
```

## Bibliotecas e APIs Usadas

| Area | Biblioteca / API | Status na POC |
| --- | --- | --- |
| UI | Kotlin + Jetpack Compose + Material 3 | Usado na tela simples da POC |
| Selecao de arquivos | Storage Access Framework via `ActivityResultContracts.OpenDocument` | Usado |
| Escrita de arquivos | `ActivityResultContracts.CreateDocument` + `ContentResolver` | Usado |
| Imagem para PDF | `BitmapFactory`, `Bitmap`, `PdfDocument` | Implementado |
| PDF para imagem | `PdfRenderer`, `Bitmap`, `Bitmap.compress`, `DocumentFile` | Implementado como alternativa Android-native multipagina |
| DOCX probe | Apache POI `poi-ooxml` | Incluido para teste minimo de abertura |
| DOCX para PDF | Nenhuma conversao final | Nao implementado nesta POC |
| PDF para DOCX | Nenhuma conversao final | Marcado como experimental/desktop-only |

## O Que Funcionou no Codigo da POC

### 1. Imagem para PDF

Implementado em:

```text
android-poc/app/src/main/java/com/documentconverter/androidpoc/poc/AndroidPocConverters.kt
```

Fluxo:

1. Usuario seleciona imagem via SAF.
2. App abre a imagem com `ContentResolver.openInputStream`.
3. App decodifica com `BitmapFactory.decodeStream`.
4. App cria PDF com `android.graphics.pdf.PdfDocument`.
5. App salva em URI escolhida pelo usuario via `ContentResolver.openOutputStream`.

Resultado esperado em dispositivo Android:

- Deve funcionar para imagens comuns suportadas pelo Android.
- Preserva a imagem em uma pagina PDF.
- Nao usa PDFBox, AWT, ImageIO, JavaFX, LibreOffice ou `File` direto.

Limites:

- Ainda nao faz paginacao multipla.
- Imagens muito grandes podem consumir memoria.
- A pagina PDF usa o tamanho do bitmap original.

### 2. PDF para Imagem

Implementado com `PdfRenderer` nativo, em vez de PDFBox-Android.

Motivo:

- `PdfRenderer` e API Android oficial.
- Evita AWT e `BufferedImage`.
- E suficiente para validar a conversao de pelo menos a primeira pagina.

Fluxo atualizado:

1. Usuario seleciona PDF via SAF.
2. Usuario escolhe uma pasta de destino via SAF.
3. App abre o PDF com `ContentResolver.openFileDescriptor`.
4. App usa `PdfRenderer` para detectar `pageCount`.
5. App percorre todas as paginas.
6. App renderiza uma pagina por vez para `Bitmap`.
7. App salva cada pagina via `Bitmap.compress` e `DocumentFile`.

Resultado esperado em dispositivo Android:

- Deve renderizar todas as paginas em PNG.
- Arquivos sao salvos como `pagina_1.png`, `pagina_2.png`, `pagina_3.png`.
- A tela exibe progresso `Pagina X de Y`.
- Permite medir qualidade e tempo em aparelho/emulador.

Limites:

- PDFs protegidos por senha podem falhar.
- Qualidade/performance dependem do tamanho da pagina e escala usada.
- Se futuramente for necessario extrair texto/estrutura do PDF, `PdfRenderer` nao substitui uma biblioteca de parsing.
- Arquivos grandes ainda precisam ser retestados em emulador/aparelho real.

Melhorias para arquivo grande:

- Processamento de uma pagina por vez.
- Fechamento imediato de cada `PdfRenderer.Page`.
- Reciclagem de `Bitmap` em `finally`.
- Limite de lado maximo do bitmap renderizado.
- Logs em Logcat com tag `AndroidPocConverters`.
- Tratamento especifico para `OutOfMemoryError`.

Tratamento de PDF protegido:

- A POC captura erros de senha/criptografia/protecao.
- Mensagem exibida:

```text
Este PDF esta protegido por senha. A versao Android ainda nao suporta PDFs protegidos.
```

### 3. DOCX para PDF / Apache POI

Implementado apenas um probe de abertura de DOCX com Apache POI.

Fluxo:

1. Usuario seleciona DOCX via SAF.
2. App abre o documento com `ContentResolver.openInputStream`.
3. App instancia `XWPFDocument`.
4. App conta paragrafos, tabelas e imagens.

Resultado esperado:

- Valida se Apache POI consegue abrir um DOCX simples no Android.

Decisao atual:

- DOCX -> PDF nao fara parte do Android MVP inicial.
- O probe com Apache POI fica apenas como experimento tecnico.

Motivos:

- Esta POC nao converte DOCX para PDF.
- Apache POI e pesado.
- Pode aumentar bastante o APK.
- Pode exigir ajustes de empacotamento, R8/ProGuard e multidex.
- Nao preserva layout; apenas valida leitura do arquivo.
- LibreOffice Headless continua inviavel no Android.

### 4. PDF para DOCX

Status: nao implementado na POC.

Conclusao tecnica inicial:

- Deve permanecer `desktop-only` ou `experimental` no Android.
- O caminho Android exigiria parser PDF compativel, reconstrucao de paragrafos/titulos/imagens e geracao DOCX.
- A chance de perda de formatacao e alta.
- Para produto real, considerar backend ou limitar o recurso no Android.

## Tamanho do APK

Status atual: medido com build debug.

Comandos executados:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android-poc"
gradle :app:assembleDebug
Get-Item .\app\build\outputs\apk\debug\app-debug.apk
```

Resultado:

```text
app-debug.apk: 25.098.935 bytes, aproximadamente 23,9 MiB
```

Observacao:

- Este tamanho inclui Compose, Material 3 e Apache POI.
- Ainda e necessario comparar com uma build sem `org.apache.poi:poi-ooxml` para medir o impacto isolado do POI.

## O Que Nao Funcionou / Bloqueios

### Teste real em emulador

Resultados informados:

- Imagem -> PDF: funcionou.
- PDF -> Imagem: funcionou parcialmente no teste inicial.
- DOCX simples: nao funcionou.
- PDF protegido: nao abriu por exigir senha.
- Arquivo grande: nao funcionou.
- Erro principal: PDF -> Imagem convertia apenas a primeira pagina.

Acao tomada:

- PDF -> Imagem foi corrigido para multipagina.
- DOCX foi removido do escopo do MVP Android inicial.
- PDF protegido foi documentado como limitacao conhecida.
- Arquivo grande recebeu melhorias de memoria/logs, mas ainda precisa de novo teste real.

### Build Android local

Comando executado:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android-poc"
gradle :app:assembleDebug
```

Resultado anterior, antes da instalacao do Gradle:

```text
gradle: The term 'gradle' is not recognized as a name of a cmdlet, function, script file, or executable program.
```

Status atualizado:

- Gradle 9.5.1 foi instalado localmente em `C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1`.
- O PATH do usuario foi atualizado com `C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1\bin`.
- Apos abrir um novo terminal, o comando `gradle --version` deve estar disponivel.

Nova tentativa de build:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android-poc"
gradle :app:assembleDebug
```

Resultado intermediario, antes da configuracao do SDK:

```text
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable
or by setting the sdk.dir path in android-poc/local.properties.
```

Status final da configuracao local:

- Android Command Line Tools instalado em `C:\Projetinhos\Projeto conversor pdf\.tools\android-sdk\cmdline-tools\latest`.
- `ANDROID_HOME` configurado para `C:\Projetinhos\Projeto conversor pdf\.tools\android-sdk`.
- `ANDROID_SDK_ROOT` configurado para `C:\Projetinhos\Projeto conversor pdf\.tools\android-sdk`.
- PATH do usuario atualizado com `cmdline-tools\latest\bin` e `platform-tools`.
- `local.properties` criado em `android-poc/local.properties`.
- Pacotes instalados:
  - `platform-tools`
  - `platforms;android-36`
  - `build-tools;36.1.0`

Resultado:

- `gradle :app:assembleDebug` executado com sucesso.
- APK gerado em `android-poc/app/build/outputs/apk/debug/app-debug.apk`.

## Recomendacao Final

1. Prosseguir com Android usando UI propria em Kotlin + Compose.
2. Implementar Imagem para PDF no Android com `PdfDocument`; e o caminho mais promissor.
3. Implementar PDF para Imagem com `PdfRenderer` multipagina; avaliar PDFBox-Android apenas se precisar de parsing mais avancado.
4. Escopo do Android MVP deve incluir apenas:
   - Imagem -> PDF.
   - PDF -> Imagem.
5. Nao incluir inicialmente:
   - DOCX -> PDF.
   - PDF -> DOCX.
6. Rodar novo teste no emulador com:
   - PDF multipagina;
   - PDF protegido;
   - arquivo grande;
   - diferentes tamanhos de pagina.

## Fontes Oficiais Consultadas

- Android Gradle Plugin 9.2: https://developer.android.com/build/releases/gradle-plugin
- Jetpack Compose BOM: https://developer.android.com/develop/ui/compose/bom
- Storage Access Framework: https://developer.android.com/guide/topics/providers/document-provider
- PdfDocument: https://developer.android.com/reference/android/graphics/pdf/PdfDocument
- PdfRenderer: https://developer.android.com/reference/android/graphics/pdf/PdfRenderer
