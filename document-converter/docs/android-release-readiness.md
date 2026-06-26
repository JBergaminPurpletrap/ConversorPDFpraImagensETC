# Document Converter Android - Relatorio de Prontidao para Publicacao

## Status

Release candidate gerado com sucesso.

## Escopo da versao 1.0.0

Incluido:

- Imagem para PDF.
- PDF para Imagem.
- Compartilhamento de resultados.
- Abertura de resultados.
- Splash Screen.
- Icone oficial adaptativo.
- Logs locais.

Fora do escopo:

- DOCX para PDF.
- PDF para DOCX.
- PDF protegido por senha.

## Qualidade

Checklist tecnico criado em:

```text
docs/android-quality-checklist.md
```

## Privacidade

Politica criada em:

```text
docs/privacy-policy.md
```

Resumo:

- Processamento local.
- Sem servidores.
- Sem coleta de dados pessoais.
- Sem conta obrigatoria.

## Play Store

Checklist criado em:

```text
docs/play-store-checklist.md
```

## Release

Artefatos esperados:

- APK debug.
- APK release assinado.
- AAB release assinado.

Artefatos gerados em 2026-06-13:

| Artefato | Caminho | Tamanho |
| --- | --- | --- |
| APK debug | `android/app/build/outputs/apk/debug/app-debug.apk` | 11,15 MB |
| APK release | `android/app/build/outputs/apk/release/app-release.apk` | 0,97 MB |
| AAB release | `android/app/build/outputs/bundle/release/app-release.aab` | 2,26 MB |

Comando executado:

```powershell
& "C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1\bin\gradle.bat" :app:assembleDebug :app:assembleRelease :app:bundleRelease
```

Resultado:

```text
BUILD SUCCESSFUL
```

## Observacoes

A keystore criada nesta fase e uma chave local de release candidate. Para publicacao definitiva, armazenar a chave com seguranca e garantir backup. Perder a chave pode impedir atualizacoes futuras fora do Play App Signing.
