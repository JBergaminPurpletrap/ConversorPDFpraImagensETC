param(
    [switch]$SkipBuild,
    [string]$LibreOfficeBundleDir = $env:DOCUMENT_CONVERTER_LIBREOFFICE_DIR,
    [ValidateSet("exe", "app-image")]
    [string]$PackageType = "exe",
    [string]$CertificatePath = $env:DOCUMENT_CONVERTER_SIGN_CERT,
    [string]$CertificatePassword = $env:DOCUMENT_CONVERTER_SIGN_PASSWORD,
    [string]$TimestampUrl = "http://timestamp.digicert.com"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$BuildScript = Join-Path $PSScriptRoot "build.ps1"
$InputDir = Join-Path $ProjectRoot "target\dist\lib"
$InstallerDir = Join-Path $ProjectRoot "target\installer"
$IconPath = Join-Path $ProjectRoot "desktop\src\main\resources\icon.ico"
$FinalInstaller = Join-Path $InstallerDir "DocumentConverterSetup.exe"
$DefaultWixBinDir = "C:\Program Files (x86)\WiX Toolset v3.14\bin"
$DefaultWindowsKits = @(
    "C:\Program Files (x86)\Windows Kits\10\bin",
    "C:\Program Files\Windows Kits\10\bin"
)

function Resolve-SignTool {
    $command = Get-Command signtool.exe -ErrorAction SilentlyContinue
    if ($null -ne $command) {
        return $command.Source
    }

    foreach ($kitRoot in $DefaultWindowsKits) {
        if (-not (Test-Path $kitRoot)) {
            continue
        }
        $candidate = Get-ChildItem -Path $kitRoot -Recurse -Filter signtool.exe -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -like "*\x64\signtool.exe" } |
            Sort-Object FullName -Descending |
            Select-Object -First 1
        if ($null -ne $candidate) {
            return $candidate.FullName
        }
    }

    return $null
}

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw "jpackage não encontrado. Instale/use um JDK 21 completo e confirme que JAVA_HOME\bin está no PATH."
}

if ((Test-Path (Join-Path $DefaultWixBinDir "candle.exe")) -and
    (Test-Path (Join-Path $DefaultWixBinDir "light.exe")) -and
    ($env:PATH -notlike "*$DefaultWixBinDir*")) {
    $env:PATH = "$DefaultWixBinDir;$env:PATH"
}

if ($PackageType -eq "exe" -and
    (-not (Get-Command candle.exe -ErrorAction SilentlyContinue) -or
     -not (Get-Command light.exe -ErrorAction SilentlyContinue))) {
    throw "WiX Toolset não encontrado. Instale o WiX 3.x e adicione candle.exe e light.exe ao PATH para gerar .exe. Para testar sem instalador, use: .\scripts\package-windows.ps1 -PackageType app-image"
}

if (-not (Test-Path $IconPath)) {
    throw "Ícone não encontrado: $IconPath"
}

if (-not $SkipBuild) {
    if ([string]::IsNullOrWhiteSpace($LibreOfficeBundleDir)) {
        & $BuildScript -SkipClean
    } else {
        & $BuildScript -SkipClean -LibreOfficeBundleDir $LibreOfficeBundleDir
    }
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

$JPackageArgs = @(
    "--type", $PackageType,
    "--name", "Document Converter",
    "--app-version", "1.0.0",
    "--vendor", "Document Converter",
    "--description", "Conversor desktop de imagens e PDFs.",
    "--input", $InputDir,
    "--main-jar", "document-converter-desktop-1.0.0.jar",
    "--main-class", "com.documentconverter.desktop.javafx.Launcher",
    "--dest", $InstallerDir,
    "--icon", $IconPath,
    "--java-options", "-Dfile.encoding=UTF-8"
)

if ($PackageType -eq "exe") {
    $JPackageArgs += @(
        "--win-upgrade-uuid", "9f9cc1e5-d0d6-4c74-9a8a-6db0fe26132f",
        "--win-dir-chooser",
        "--win-menu",
        "--win-shortcut"
    )
}

& jpackage @JPackageArgs

if ($LASTEXITCODE -ne 0) {
    throw "jpackage falhou com código $LASTEXITCODE."
}

if ($PackageType -eq "app-image") {
    Write-Host "App image gerada em: $InstallerDir"
    Write-Host "Executável esperado em: $InstallerDir\Document Converter\Document Converter.exe"
    return
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

$FinalInstallerItem = Get-Item -LiteralPath $FinalInstaller
if ($FinalInstallerItem.IsReadOnly) {
    $FinalInstallerItem.IsReadOnly = $false
}

if (-not [string]::IsNullOrWhiteSpace($CertificatePath)) {
    if (-not (Test-Path $CertificatePath)) {
        throw "Certificado não encontrado: $CertificatePath"
    }
    if ([string]::IsNullOrWhiteSpace($CertificatePassword)) {
        throw "Senha do certificado não informada. Use -CertificatePassword ou DOCUMENT_CONVERTER_SIGN_PASSWORD."
    }

    $SignTool = Resolve-SignTool
    if ($null -eq $SignTool) {
        throw "signtool.exe não encontrado. Instale o Windows SDK ou adicione signtool.exe ao PATH."
    }

    & $SignTool sign `
        /fd SHA256 `
        /f $CertificatePath `
        /p $CertificatePassword `
        /tr $TimestampUrl `
        /td SHA256 `
        $FinalInstaller

    if ($LASTEXITCODE -ne 0) {
        throw "Assinatura do instalador falhou com código $LASTEXITCODE."
    }

    & $SignTool verify /pa /v $FinalInstaller
    if ($LASTEXITCODE -ne 0) {
        throw "Verificação da assinatura falhou com código $LASTEXITCODE."
    }

    Write-Host "Instalador assinado digitalmente."
} else {
    Write-Host "Instalador não assinado. Informe -CertificatePath e -CertificatePassword para assinar."
}

Write-Host "Instalador gerado: $FinalInstaller"
