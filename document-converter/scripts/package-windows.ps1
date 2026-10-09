param(
    [switch]$SkipBuild
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BuildScript = Join-Path $PSScriptRoot "build.ps1"
$InputDir = Join-Path $ProjectRoot "target\dist\lib"
$InstallerDir = Join-Path $ProjectRoot "target\installer"
$IconPath = Join-Path $ProjectRoot "desktop\src\main\resources\icon.ico"
$FinalInstaller = Join-Path $InstallerDir "DocumentConverterSetup.exe"

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw "jpackage não encontrado. Instale/use um JDK 21 completo e confirme que JAVA_HOME\bin está no PATH."
}

if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or
    -not (Get-Command light.exe -ErrorAction SilentlyContinue)) {
    throw "WiX Toolset não encontrado. Instale o WiX 3.x e adicione candle.exe e light.exe ao PATH para gerar .exe."
}

if (-not (Test-Path $IconPath)) {
    throw "Ícone não encontrado: $IconPath"
}

if (-not $SkipBuild) {
    & $BuildScript
    if ($LASTEXITCODE -ne 0) {
        throw "Build falhou com código $LASTEXITCODE."
    }
}

if (-not (Test-Path $InputDir)) {
    throw "Diretório de entrada não encontrado: $InputDir"
}

if (Test-Path $InstallerDir) {
    Remove-Item -LiteralPath $InstallerDir -Recurse -Force
}
New-Item -ItemType Directory -Path $InstallerDir | Out-Null

jpackage `
    --type exe `
    --name "Document Converter" `
    --app-version "1.0.0" `
    --vendor "Document Converter" `
    --description "Conversor desktop de imagens e PDFs." `
    --win-upgrade-uuid "9f9cc1e5-d0d6-4c74-9a8a-6db0fe26132f" `
    --input $InputDir `
    --main-jar "document-converter-desktop-1.0.0.jar" `
    --main-class "com.documentconverter.desktop.javafx.App" `
    --dest $InstallerDir `
    --icon $IconPath `
    --win-dir-chooser `
    --win-menu `
    --win-shortcut `
    --java-options "-Dfile.encoding=UTF-8"

if ($LASTEXITCODE -ne 0) {
    throw "jpackage falhou com código $LASTEXITCODE."
}

$GeneratedInstaller = Get-ChildItem -Path $InstallerDir -Filter "*.exe" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $GeneratedInstaller) {
    throw "Nenhum instalador .exe foi gerado em: $InstallerDir"
}

if (Test-Path $FinalInstaller) {
    Remove-Item -LiteralPath $FinalInstaller -Force
}

Move-Item -LiteralPath $GeneratedInstaller.FullName -Destination $FinalInstaller
Write-Host "Instalador gerado: $FinalInstaller"
