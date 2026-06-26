# Document Converter Android - Plano do MVP

## Objetivo

Criar o aplicativo Android final em `document-converter/android`, mantendo `android-poc` apenas como prova de conceito.

O MVP Android inclui somente as conversoes validadas em teste real:

- Imagem para PDF
- PDF para Imagem

## Escopo incluido

### Imagem para PDF

- Seleciona uma ou varias imagens com Storage Access Framework.
- Aceita `image/jpeg`, `image/png` e `image/webp`.
- Exibe quantidade e lista de imagens selecionadas.
- Gera um PDF unico com uma imagem por pagina.
- Salva o PDF com `ActivityResultContracts.CreateDocument`.
- Processa uma imagem por vez para reduzir uso de memoria.

### PDF para Imagem

- Seleciona PDF com Storage Access Framework.
- Verifica quantidade de paginas usando `PdfRenderer`.
- Renderiza todas as paginas.
- Salva imagens PNG em uma pasta selecionada com `OpenDocumentTree`.
- Nomeia os arquivos como:
  - `pagina_1.png`
  - `pagina_2.png`
  - `pagina_3.png`
- Processa uma pagina por vez e recicla bitmaps.
- Exibe mensagem amigavel para PDF protegido por senha.

## Fora do MVP Android inicial

- DOCX para PDF.
- PDF para DOCX.
- Desbloqueio de PDF protegido por senha.

Motivos:

- Apache POI e pesado no Android.
- Conversao DOCX para PDF com preservacao de layout e complexa no Android.
- LibreOffice Headless nao funciona no Android.
- `PdfRenderer` nativo do Android nao oferece API para informar senha e desbloquear PDF protegido.

## Estrutura criada

```text
android/
├── app/
│   └── src/main/java/com/documentconverter/android/
│       ├── MainActivity.kt
│       ├── converter/
│       │   ├── ConversionResult.kt
│       │   ├── ImageToPdfConverter.kt
│       │   └── PdfToImageConverter.kt
│       ├── model/
│       │   ├── AppScreen.kt
│       │   ├── ConversionState.kt
│       │   └── SelectedDocument.kt
│       ├── ui/
│       │   ├── components/
│       │   ├── screens/
│       │   └── theme/
│       ├── util/
│       │   └── DocumentNameResolver.kt
│       └── viewmodel/
│           ├── ImageToPdfViewModel.kt
│           └── PdfToImageViewModel.kt
```

## Arquitetura

O app usa MVVM simples:

- `ui/screens`: telas Compose.
- `viewmodel`: estado da tela, selecao de documentos e chamadas de conversao.
- `converter`: regras de conversao Android.
- `model`: modelos de estado e navegacao.
- `util`: utilitarios Android, como resolver nome de documento.

As telas nao fazem conversao diretamente. Elas apenas disparam seletores SAF, exibem estado e chamam o ViewModel.

## Reaproveitamento da POC

Migrado de `android-poc`:

- Conversao Imagem para PDF com `PdfDocument`.
- Conversao PDF para Imagem com `PdfRenderer`.
- Uso de `ContentResolver`.
- Uso de `DocumentFile` para pasta de saida.
- Processamento pagina por pagina.
- Tratamento de `OutOfMemoryError`.
- Mensagem amigavel para PDF protegido.

Removido do app final:

- Teste DOCX com Apache POI.
- Dependencia `org.apache.poi:poi-ooxml`.
- Tela de laboratorio da POC.

## Comandos

Build debug:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android"
gradle :app:assembleDebug
```

Instalar no emulador:

```powershell
adb install -r "C:\Projetinhos\Projeto conversor pdf\document-converter\android\app\build\outputs\apk\debug\app-debug.apk"
```

## Checklist de teste manual

- Abrir app no emulador.
- Entrar em Imagem para PDF.
- Selecionar 1 imagem.
- Gerar PDF.
- Selecionar varias imagens.
- Gerar PDF multipagina.
- Entrar em PDF para Imagem.
- Selecionar PDF pequeno.
- Escolher pasta de destino.
- Confirmar criacao de `pagina_1.png`, `pagina_2.png` etc.
- Testar PDF protegido e confirmar mensagem amigavel.
- Testar PDF grande validado na POC.
