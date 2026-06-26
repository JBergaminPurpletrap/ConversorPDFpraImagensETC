# Distribuição Windows

Este projeto usa Maven, JavaFX e `jpackage` para gerar um instalador Windows com runtime Java embutido.

## Pré-requisitos

- Windows 10 ou superior.
- JDK 21 completo instalado.
- `JAVA_HOME` apontando para o JDK 21.
- `jpackage` disponível no `PATH`.
- WiX Toolset 3.x instalado e disponível no `PATH` para gerar instaladores `.exe`.
- Windows SDK instalado para assinatura digital com `signtool.exe`, se for assinar o instalador.

Verificações:

```powershell
java -version
jpackage --version
candle.exe -?
light.exe -?
.\mvnw.cmd -version
signtool.exe /?
```

Instalação do WiX via winget, em PowerShell aberto como Administrador:

```powershell
winget install --id WiXToolset.WiXToolset --exact --accept-package-agreements --accept-source-agreements
```

Se o instalador informar dependência do recurso `NetFx3`, habilite o .NET Framework 3.5 pelo Windows ou execute, também como Administrador:

```powershell
Dism /Online /Enable-Feature /FeatureName:NetFx3 /All
```

Depois feche e abra novamente o terminal/VS Code e confirme:

```powershell
candle.exe -?
light.exe -?
```

## Gerar build da aplicação

Na pasta `document-converter`, execute:

```powershell
.\scripts\build.ps1
```

Esse script executa o Maven no módulo `desktop`, gera o JAR da aplicação e copia as dependências de runtime para:

```text
target\dist\lib
```

## Gerar instalador

Na pasta `document-converter`, execute:

```powershell
.\scripts\package-windows.ps1
```

Resultado esperado:

```text
target\installer\DocumentConverterSetup.exe
```

Para gerar o `.exe`, o Windows precisa do WiX Toolset 3.x no `PATH`, com:

```text
candle.exe
light.exe
```

Se o WiX ainda não estiver instalado, o script mostrará uma mensagem explicando isso.

## Testar sem WiX

Enquanto o WiX não estiver instalado, é possível gerar uma pasta executável com `jpackage` usando `app-image`.
Esse modo não cria instalador, mas permite testar a aplicação empacotada:

```powershell
.\scripts\package-windows.ps1 -PackageType app-image
```

Com LibreOffice embutido:

```powershell
.\scripts\package-windows.ps1 -PackageType app-image -LibreOfficeBundleDir "C:\Program Files\LibreOffice"
```

Executável esperado:

```text
target\installer\Document Converter\Document Converter.exe
```

O instalador inclui:

- Instalação padrão Windows.
- Atalho na área de trabalho.
- Entrada no menu iniciar.
- Ícone personalizado.
- Runtime Java incluído automaticamente pelo `jpackage`.
- Opcionalmente, LibreOffice embutido para DOCX -> PDF funcionar sem instalação separada.

## LibreOffice para DOCX -> PDF

Não existe um `import` Java simples que substitua o LibreOffice para preservar bem a formatação de DOCX.
A conversão DOCX -> PDF usa o executável nativo `soffice.exe`.

O app procura o LibreOffice nesta ordem:

- Caminho configurado pelo usuário na tela DOCX -> PDF.
- Propriedade Java `documentconverter.libreoffice.path`.
- Variável `LIBREOFFICE_PATH`.
- Variável `LIBREOFFICE_HOME`.
- Caminhos padrão do Windows.
- `PATH` do Windows.
- LibreOffice embutido ao lado dos JARs do aplicativo:

```text
libreoffice\program\soffice.exe
```

### Gerar instalador com LibreOffice embutido

Para que o usuário instale apenas `DocumentConverterSetup.exe` e o DOCX -> PDF já funcione, forneça ao script uma pasta raiz do LibreOffice contendo:

```text
program\soffice.exe
```

Exemplo usando uma instalação local existente:

```powershell
.\scripts\package-windows.ps1 -LibreOfficeBundleDir "C:\Program Files\LibreOffice"
```

Ou configure uma variável de ambiente:

```powershell
$env:DOCUMENT_CONVERTER_LIBREOFFICE_DIR = "C:\Program Files\LibreOffice"
.\scripts\package-windows.ps1
```

O script copiará essa pasta para:

```text
target\dist\lib\libreoffice
```

Depois o `jpackage` incluirá essa pasta no instalador.

Observação: incluir o LibreOffice aumenta bastante o tamanho do instalador. Antes de distribuir, valide a licença e mantenha os avisos/licenças do LibreOffice junto com a distribuição.

## OCR desktop para PDF -> DOCX

O desktop tenta usar OCR automaticamente em paginas de PDF sem texto selecionavel.

Motor usado:

```text
Tesseract OCR via tesseract.exe
```

Ordem de deteccao:

- Propriedade Java `documentconverter.tesseract.path`.
- Variavel `TESSERACT_CMD`.
- Variavel `TESSERACT_HOME`.
- Caminho padrao `C:\Program Files\Tesseract-OCR\tesseract.exe`.
- `PATH` do Windows.

Idiomas padrao:

```text
por+eng
```

Para alterar:

```powershell
$env:TESSERACT_LANGUAGES = "por+eng"
```

Se o Tesseract nao estiver instalado/configurado, o app continua convertendo normalmente, mas paginas escaneadas podem sair sem texto.

## Assinar instalador Windows

Para assinar o `.exe`, e necessario um certificado de code signing em `.pfx`.

Exemplo:

```powershell
.\scripts\package-windows.ps1 `
  -LibreOfficeBundleDir "C:\Program Files\LibreOffice" `
  -CertificatePath "C:\certificados\document-converter-code-signing.pfx" `
  -CertificatePassword "SENHA_DO_CERTIFICADO"
```

Tambem e possivel usar variaveis de ambiente:

```powershell
$env:DOCUMENT_CONVERTER_SIGN_CERT = "C:\certificados\document-converter-code-signing.pfx"
$env:DOCUMENT_CONVERTER_SIGN_PASSWORD = "SENHA_DO_CERTIFICADO"
.\scripts\package-windows.ps1 -LibreOfficeBundleDir "C:\Program Files\LibreOffice"
```

O script usa `signtool.exe` com SHA-256 e timestamp:

```text
http://timestamp.digicert.com
```

Sem certificado real, o instalador e gerado sem assinatura digital.

## Testar instalador

1. Execute:

```powershell
.\target\installer\DocumentConverterSetup.exe
```

2. Conclua a instalação.
3. Abra o app pelo atalho da área de trabalho ou pelo menu iniciar.
4. Teste:

- Converter imagens para PDF.
- Converter PDF para imagens.
- Arrastar arquivos para as telas de conversão.
- Abrir a tela Histórico.
- Confirmar criação/atualização do arquivo `history\conversion-history.json`.

## Distribuir para usuários

Envie apenas o arquivo:

```text
DocumentConverterSetup.exe
```

Recomendações:

- Distribuir por um canal confiável, como site oficial, intranet ou armazenamento corporativo.
- Informar que o instalador já inclui Java, então o usuário não precisa instalar JDK/JRE.
- Antes de publicar uma versão, testar o instalador em uma máquina limpa ou VM Windows.

## Comandos principais

```powershell
.\mvnw.cmd clean install "-pl" "desktop" "-am"
.\mvnw.cmd dependency:copy-dependencies "-pl" "core" "-DincludeScope=runtime" "-DoutputDirectory=target\dist\lib"
.\mvnw.cmd dependency:copy-dependencies "-pl" "desktop" "-DincludeScope=runtime" "-DexcludeGroupIds=com.documentconverter" "-DoutputDirectory=target\dist\lib"
Copy-Item "C:\Program Files\LibreOffice" "target\dist\lib\libreoffice" -Recurse -Force
jpackage --type exe --name "Document Converter" --app-version "1.0.0" --vendor "Document Converter" --description "Conversor desktop de imagens e PDFs." --win-upgrade-uuid "9f9cc1e5-d0d6-4c74-9a8a-6db0fe26132f" --input target\dist\lib --main-jar document-converter-desktop-1.0.0.jar --main-class com.documentconverter.desktop.javafx.Launcher --dest target\installer --icon desktop\src\main\resources\icon.ico --win-dir-chooser --win-menu --win-shortcut --java-options "-Dfile.encoding=UTF-8"
```

## Solução de problemas

Se o empacotamento falhar com:

```text
Can not find WiX tools (light.exe, candle.exe)
Error: Invalid or unsupported type: [exe]
```

Instale o WiX Toolset 3.x e adicione a pasta dos executáveis ao `PATH`. Depois abra um novo terminal e confirme:

```powershell
candle.exe -?
light.exe -?
```
