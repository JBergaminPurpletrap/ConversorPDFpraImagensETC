param(
    [switch]$SkipClean
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$DistLibDir = Join-Path $ProjectRoot "target\dist\lib"
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

    Write-Host "Build pronto em: $DistLibDir"
} finally {
    Pop-Location
}
