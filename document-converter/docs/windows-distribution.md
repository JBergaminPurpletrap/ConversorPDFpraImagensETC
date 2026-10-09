# Distribuição Windows

Este projeto usa Maven, JavaFX e `jpackage` para gerar um instalador Windows com runtime Java embutido.

## Pré-requisitos

- Windows 10 ou superior.
- JDK 21 completo instalado.
- `JAVA_HOME` apontando para o JDK 21.
- `jpackage` disponível no `PATH`.
- WiX Toolset 3.x instalado e disponível no `PATH` para gerar instaladores `.exe`.

Verificações:

```powershell
java -version
jpackage --version
candle.exe -?
light.exe -?
.\mvnw.cmd -version
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

O instalador inclui:

- Instalação padrão Windows.
- Atalho na área de trabalho.
- Entrada no menu iniciar.
- Ícone personalizado.
- Runtime Java incluído automaticamente pelo `jpackage`.

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
jpackage --type exe --name "Document Converter" --app-version "1.0.0" --vendor "Document Converter" --description "Conversor desktop de imagens e PDFs." --win-upgrade-uuid "9f9cc1e5-d0d6-4c74-9a8a-6db0fe26132f" --input target\dist\lib --main-jar document-converter-desktop-1.0.0.jar --main-class com.documentconverter.desktop.javafx.App --dest target\installer --icon desktop\src\main\resources\icon.ico --win-dir-chooser --win-menu --win-shortcut --java-options "-Dfile.encoding=UTF-8"
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
