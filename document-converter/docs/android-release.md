# Document Converter Android - Build e Distribuicao

## Situacao atual

A Fase 15 prepara uma versao candidata para distribuicao:

- APK debug para testes.
- APK release assinado.
- AAB release assinado.

## Requisitos locais

- Android SDK configurado em `.tools/android-sdk`.
- Gradle configurado em `.tools/gradle-9.5.1`.
- Plataforma Android instalada: API 36.
- Build Tools instaladas: 36.1.0.

O arquivo `android/local.properties` aponta para o SDK local:

```properties
sdk.dir=C:/Projetinhos/Projeto conversor pdf/.tools/android-sdk
```

## Gerar APK debug

Comando recomendado usando o Gradle local:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android"
& "C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1\bin\gradle.bat" :app:assembleDebug
```

Se `gradle` estiver no PATH, tambem funciona:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android"
gradle :app:assembleDebug
```

## Local do APK debug

```text
C:\Projetinhos\Projeto conversor pdf\document-converter\android\app\build\outputs\apk\debug\app-debug.apk
```

Tamanho observado apos incluir OCR offline com ML Kit:

```text
app-debug.apk: 66.680.402 bytes
```

## Gerar APK release

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android"
& "C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1\bin\gradle.bat" :app:assembleRelease
```

Arquivo gerado:

```text
C:\Projetinhos\Projeto conversor pdf\document-converter\android\app\build\outputs\apk\release\app-release.apk
```

Tamanho observado apos incluir OCR offline com ML Kit:

```text
app-release.apk: 50.200.667 bytes
```

Observacao:

- O OCR usa o modelo bundled do ML Kit, entao o app funciona localmente e sem internet para reconhecimento de texto latino.
- O custo e aumento do APK por incluir o modelo dentro do aplicativo.

## Gerar AAB release

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android"
& "C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1\bin\gradle.bat" :app:bundleRelease
```

Arquivo gerado:

```text
C:\Projetinhos\Projeto conversor pdf\document-converter\android\app\build\outputs\bundle\release\app-release.aab
```

## Assinatura

A configuracao de assinatura release usa arquivo local fora do projeto:

```text
C:\Projetinhos\Projeto conversor pdf\.local\document-converter-android\release-keystore.properties
C:\Projetinhos\Projeto conversor pdf\.local\document-converter-android\document-converter-release.jks
```

Tambem e possivel assinar usando variaveis de ambiente:

```text
DOCUMENT_CONVERTER_STORE_FILE
DOCUMENT_CONVERTER_STORE_PASSWORD
DOCUMENT_CONVERTER_KEY_ALIAS
DOCUMENT_CONVERTER_KEY_PASSWORD
```

A keystore e uma chave local de release candidate. Para publicacao definitiva, guardar a chave com seguranca e manter backup.

## Instalar no emulador

Com o emulador aberto:

```powershell
adb install -r "C:\Projetinhos\Projeto conversor pdf\document-converter\android\app\build\outputs\apk\debug\app-debug.apk"
```

Se o `adb` nao estiver no PATH, usar o executavel do SDK local:

```powershell
& "C:\Projetinhos\Projeto conversor pdf\.tools\android-sdk\platform-tools\adb.exe" install -r "C:\Projetinhos\Projeto conversor pdf\document-converter\android\app\build\outputs\apk\debug\app-debug.apk"
```

## Testar no emulador

1. Abrir o app `Document Converter`.
2. Testar `Imagem para PDF` com uma imagem.
3. Testar `Imagem para PDF` com varias imagens.
4. Testar `PDF para Imagem` com PDF pequeno.
5. Testar `PDF para Imagem` com PDF grande.
6. Testar PDF protegido por senha em `PDF para Imagem` e `PDF para DOCX`.
7. Testar `PDF para DOCX` com PDF escaneado para validar OCR experimental.
8. Verificar se PDF protegido com senha correta converte e senha incorreta mostra erro amigavel.

O `PdfRenderer` nativo do Android nao abre PDF com senha. Para os fluxos protegidos, o app usa PDFBox-Android como fallback.

## Publicacao futura

Antes de publicar:

- Testar instalacao limpa do APK release.
- Testar AAB pela Play Console.
- Confirmar politica de privacidade publicada em URL publica.
- Revisar capturas e textos da loja.
- Guardar keystore com seguranca.
