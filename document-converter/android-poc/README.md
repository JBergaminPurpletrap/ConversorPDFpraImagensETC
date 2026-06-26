# Document Converter Android POC

Prova de conceito minima para validar compatibilidade Android antes de criar o app final.

## Requisitos locais

- Android Studio com Android SDK instalado.
- JDK 17 ou superior para o Android Gradle Plugin.
- Gradle 9.4.1 ou superior, conforme requisito do AGP 9.2.

## Build

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android-poc"
gradle :app:assembleDebug
```

## Escopo

- Imagem para PDF com `BitmapFactory`, `Bitmap`, `PdfDocument`, `ContentResolver` e SAF.
- PDF para imagem com `PdfRenderer`, `Bitmap`, `ContentResolver` e SAF.
- DOCX probe com Apache POI para validar abertura de documento simples.
- PDF para DOCX documentado como experimental/desktop-only nesta POC.
