# Funcionalidades por Plataforma

Atualizado em: 2026-06-19

## Resumo

| Funcionalidade | Desktop | Android |
| --- | --- | --- |
| Imagem -> PDF | Funciona | Funciona |
| PDF -> Imagem | Funciona | Funciona |
| PDF protegido por senha | Funciona para PDF -> Imagem/PDF -> DOCX com senha | Funciona para PDF -> Imagem/PDF -> DOCX com senha |
| DOCX -> PDF | Funciona com LibreOffice | Fora do MVP |
| PDF -> DOCX | Funciona com limitacoes | Experimental |
| Historico | Funciona | Nao implementado |
| Configuracoes | Nao implementado | Funciona |
| Drag and drop | Funciona | Nao aplicavel no mobile |
| Distribuicao | Instalador Windows `.exe` | APK debug/release |

## Desktop JavaFX

### Imagem -> PDF

Status: funcionando.

Recursos:

- Seleciona uma ou varias imagens.
- Aceita drag and drop.
- Gera PDF multipagina.
- Valida formatos invalidos.
- Ignora duplicados quando aplicavel.
- Mostra progresso.
- Registra historico.

Tecnologias:

- JavaFX.
- PDFBox.
- ImageIO/BufferedImage.

Limites:

- Depende dos formatos suportados pelo ImageIO.
- Imagens muito grandes podem consumir bastante memoria.

### PDF -> Imagem

Status: funcionando.

Recursos:

- Seleciona PDF.
- Aceita drag and drop.
- Converte paginas para imagens.
- Permite escolher formato de saida.
- Permite escolher DPI.
- Mostra progresso.
- Registra historico.

Formatos:

- JPG/JPEG.
- PNG.

Tecnologias:

- PDFBox `PDFRenderer`.
- ImageIO.

Limites:

- PDFs muito grandes podem exigir mais memoria e tempo.
- Quando o PDF exige senha, o desktop solicita a senha e tenta repetir a conversao.

### DOCX -> PDF

Status: funcionando com LibreOffice.

Recursos:

- Seleciona `.docx`.
- Salva `.pdf`.
- Usa LibreOffice Headless.
- Permite configurar manualmente `soffice.exe`.
- Detecta LibreOffice em caminhos padrao.
- Detecta LibreOffice embutido no pacote.
- Registra historico.

Tecnologia:

- LibreOffice `soffice.exe`.

Limites:

- Precisa de LibreOffice instalado ou embutido no instalador.
- Fidelidade depende do motor do LibreOffice.

### PDF -> DOCX

Status: funcionando com limitacoes.

Recursos:

- Seleciona PDF.
- Gera DOCX.
- Extrai texto e estrutura simples.
- Aplica OCR opcional via Tesseract quando a pagina nao possui texto selecionavel.
- Preserva imagens embutidas simples quando o PDF expoe imagens como recursos.
- Converte tabelas simples quando o texto extraido possui colunas/separadores detectaveis.
- Solicita senha quando o PDF protegido exige desbloqueio.
- Registra metricas e historico.

Tecnologias:

- PDFBox.
- Apache POI.
- Tesseract OCR via `tesseract.exe` opcional.

Limites:

- Layout complexo pode ser perdido.
- OCR desktop depende de Tesseract instalado/configurado.
- Tabelas complexas podem nao ser reconstruidas.

### Historico

Status: funcionando.

Recursos:

- Registra data, hora, origem, destino, tipo e status.
- Permite pesquisa por nome.
- Permite filtro por tipo.

Persistencia:

```text
history/conversion-history.json
```

### Distribuicao Windows

Status: funcionando.

Artefato:

```text
target/installer/DocumentConverterSetup.exe
```

Recursos:

- Runtime Java embutido.
- Atalho no Windows.
- Icone personalizado.
- LibreOffice opcionalmente embutido.

## Android

### Imagem -> PDF

Status: funcionando.

Recursos:

- Seleciona multiplas imagens com Storage Access Framework.
- Aceita JPG, JPEG, PNG e WEBP.
- Gera PDF multipagina.
- Processa uma imagem por vez.
- Mostra progresso.
- Usa `ContentResolver`.
- Nao usa `File` direto para arquivos do usuario.

Tecnologias:

- `BitmapFactory`.
- `PdfDocument`.
- `ContentResolver`.
- SAF.

Limites:

- Ordem depende da ordem retornada pelo seletor Android.
- Imagens muito grandes podem exigir compressao futura.

### PDF -> Imagem

Status: funcionando.

Recursos:

- Seleciona PDF com SAF.
- Seleciona pasta de saida com SAF.
- Converte todas as paginas.
- Salva imagens separadas.
- Formato padrao: JPEG.
- Opcoes: JPEG e PNG.
- Mostra progresso por pagina.
- Compartilha imagens geradas.

Tecnologias:

- `PdfRenderer` para PDFs comuns.
- PDFBox-Android como fallback para PDFs protegidos por senha.
- `DocumentFile`.
- `ContentResolver`.

Limites:

- PDFs muito complexos/protegidos podem depender do suporte do PDFBox-Android.
- JPEG2000 interno do PDFBox usa classes opcionais ignoradas no R8.

### PDF Protegido por Senha

Status: implementado para PDF -> Imagem.

Fluxo:

- App detecta falha de abertura/protecao.
- Exibe popup de senha.
- Usuario informa senha.
- Conversao tenta abrir com PDFBox-Android.
- Senha incorreta gera mensagem amigavel.

Limites:

- O `PdfRenderer` nativo nao abre PDFs com senha.
- PDFs com criptografia muito especifica podem falhar mesmo com senha correta.

### PDF -> DOCX

Status: experimental.

Recursos:

- Seleciona PDF.
- Salva DOCX via `CreateDocument`.
- Suporta senha quando detectado PDF protegido.
- Extrai texto.
- Preserva paragrafos simples.
- Preserva separacao entre paginas com quebras de pagina no DOCX.
- Infere titulos simples por texto curto em caixa alta.
- Aplica OCR experimental com ML Kit quando uma pagina nao tem texto selecionavel.
- Detecta quando mesmo o OCR nao conseguiu reconhecer texto.
- Converte tabelas simples quando o texto extraido/OCR possui colunas/separadores detectaveis.
- Compartilha/abre arquivo gerado.

Tecnologias:

- PDFBox-Android.
- `PDFTextStripper`.
- PDFBox-Android `PDFRenderer` para renderizar paginas usadas no OCR.
- ML Kit Text Recognition bundled.
- Geracao DOCX via ZIP/XML OpenXML minimo.
- SAF.

Limites:

- Nao recria layout complexo.
- Nao preserva tabelas visuais.
- Nao preserva imagens.
- OCR e experimental e depende da qualidade/resolucao da pagina.
- OCR inicialmente focado em texto latino.
- PDFs escaneados com baixa qualidade podem gerar texto incompleto ou incorreto.

### DOCX -> PDF

Status: fora do MVP Android.

Motivos:

- Apache POI e pesado.
- LibreOffice Headless nao funciona no Android.
- Conversao DOCX -> PDF com fidelidade exige motor de layout complexo.

### Configuracoes

Status: funcionando.

Recursos:

- Ativa/desativa OCR experimental no Android.
- Ajusta qualidade de OCR entre 150 e 300 DPI.
- Configuracoes persistem localmente via `SharedPreferences`.

## Builds e Artefatos

Desktop:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter"
.\mvnw.cmd -pl desktop -am compile
.\mvnw.cmd -pl desktop javafx:run
.\scripts\package-windows.ps1 -LibreOfficeBundleDir "C:\Program Files\LibreOffice"
```

Android:

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android"
& "C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1\bin\gradle.bat" :app:assembleDebug
& "C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1\bin\gradle.bat" :app:assembleRelease
```

Artefatos:

```text
android/app/build/outputs/apk/debug/app-debug.apk
android/app/build/outputs/apk/release/app-release.apk
target/installer/DocumentConverterSetup.exe
```

## Recomendacoes de Melhorias

### Prioridade Alta

- Testar Android em emulador/aparelho real com PDF protegido e senha correta.
- Testar Android com PDF protegido e senha incorreta.
- Testar PDF -> DOCX Android com PDFs reais de texto, tabela e arquivo escaneado.
- Criar arquivos de exemplo para regressao manual.
- Criar historico local no Android.

### Prioridade Media

- Melhorar PDF -> DOCX Android:
  - melhorar heuristica de titulos;
  - pesquisar suporte a imagens simples.
- Testar OCR Android com PDFs escaneados reais, fotos de documento e baixa resolucao.
- Expandir testes unitarios do `core` para PDFs protegidos e casos de erro.
- Adicionar progresso mais detalhado no DOCX -> PDF desktop.
- Criar testes automatizados de conversao com arquivos pequenos.

### Prioridade Baixa

- Melhorar OCR com selecao de idioma, revisao manual e deteccao de rotacao.
- Criar assinatura digital do instalador Windows.
- Criar pipeline de release.
- Separar `core-api`, `core-desktop` e `core-android`.
- Criar pagina de ajuda dentro dos apps.
