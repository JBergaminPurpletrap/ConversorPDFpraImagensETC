param(
    [switch]$SkipClean,
    [string]$LibreOfficeBundleDir = $env:DOCUMENT_CONVERTER_LIBREOFFICE_DIR
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$DistLibDir = Join-Path $ProjectRoot "target\dist\lib"
$DistLibreOfficeDir = Join-Path $DistLibDir "libreoffice"
$AppJar = Join-Path $ProjectRoot "desktop\target\document-converter-desktop-1.0.0.jar"
$CoreJar = Join-Path $ProjectRoot "core\target\document-converter-core-1.0.0.jar"

Push-Location $ProjectRoot
try {
    if (-not $SkipClean) {
        & .\mvnw.cmd clean install "-pl" "desktop" "-am"
    } else {
        & .\mvnw.cmd install "-pl" "desktop" "-am"
    }

    if ($LASTEXITCODE -ne 0) {
        throw "Maven package falhou com código $LASTEXITCODE."
    }

    if (Test-Path $DistLibDir) {
        Remove-Item -LiteralPath $DistLibDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $DistLibDir | Out-Null

    Copy-Item -LiteralPath $AppJar -Destination $DistLibDir
    Copy-Item -LiteralPath $CoreJar -Destination $DistLibDir

    & .\mvnw.cmd dependency:copy-dependencies "-pl" "core" "-DincludeScope=runtime" "-DoutputDirectory=$DistLibDir"
    if ($LASTEXITCODE -ne 0) {
        throw "Cópia de dependências do core falhou com código $LASTEXITCODE."
    }

    & .\mvnw.cmd dependency:copy-dependencies "-pl" "desktop" "-DincludeScope=runtime" "-DexcludeGroupIds=com.documentconverter" "-DoutputDirectory=$DistLibDir"
    if ($LASTEXITCODE -ne 0) {
        throw "Cópia de dependências do desktop falhou com código $LASTEXITCODE."
    }

    if (-not [string]::IsNullOrWhiteSpace($LibreOfficeBundleDir)) {
        $LibreOfficeBundleDir = [System.IO.Path]::GetFullPath($LibreOfficeBundleDir)
        $SofficePath = Join-Path $LibreOfficeBundleDir "program\soffice.exe"
        if (-not (Test-Path $SofficePath)) {
            throw @"
LibreOffice inválido ou não instalado no caminho informado.

Caminho recebido:
$LibreOfficeBundleDir

Arquivo esperado:
$SofficePath

Instale o LibreOffice ou informe a pasta raiz correta que contém program\soffice.exe.

Exemplo após instalar pelo instalador padrão:
.\scripts\package-windows.ps1 -LibreOfficeBundleDir "C:\Program Files\LibreOffice"

Para instalar via winget em PowerShell Administrador:
winget install --id TheDocumentFoundation.LibreOffice --exact --accept-package-agreements --accept-source-agreements
"@
        }

        Copy-Item -LiteralPath $LibreOfficeBundleDir -Destination $DistLibreOfficeDir -Recurse -Force
        Write-Host "LibreOffice embutido copiado para: $DistLibreOfficeDir"
    } else {
        Write-Host "LibreOffice embutido não configurado. DOCX -> PDF dependerá do LibreOffice instalado ou configurado no app."
    }

    Write-Host "Build pronto em: $DistLibDir"
} finally {
    Pop-Location
}
