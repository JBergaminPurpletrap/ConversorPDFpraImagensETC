Imagem -> PDF: funcionou.
PDF -> Imagem: funcionou.
DOCX simples: nao funcionou.
PDF protegido: implementado com senha usando PDFBox-Android.
Arquivo grande: funcionou.

Erro principal encontrado:

- DOCX -> PDF continua fora do MVP Android.
- PDF -> DOCX foi implementado como recurso experimental de extracao de texto.

Correcao aplicada no Android:

- Imagem -> PDF agora usa selecao multipla com `OpenMultipleDocuments`.
- Aceita `image/jpeg`, `image/png` e `image/webp`.
- Exibe quantidade e lista simples de imagens selecionadas.
- Gera um unico PDF com uma imagem por pagina.
- Mantem a ordem retornada pelo seletor SAF.
- Processa uma imagem por vez e recicla o bitmap.
- Exibe progresso:
  - `Imagem 1 de N`
  - `Imagem 2 de N`
  - `PDF criado com sucesso`
- PDF -> Imagem agora detecta o total de paginas.
- Renderiza todas as paginas com `PdfRenderer`.
- Salva cada pagina como arquivo separado em uma pasta escolhida via SAF.
- Nomeia os arquivos como:
  - `pagina_1.png`
  - `pagina_2.png`
  - `pagina_3.png`
- Exibe progresso:
  - `Pagina 1 de N`
  - `Pagina 2 de N`
  - `Conversao concluida`

Tratamento de PDF protegido:

- Ao selecionar PDF protegido, o app tenta inspecionar o arquivo.
- Se detectar protecao, exibe popup:
  - Titulo: `PDF protegido`
  - Mensagem: `Este PDF esta protegido por senha. Digite a senha para tentar abrir.`
  - Campo: `Senha`
  - Botoes: `Cancelar` e `Confirmar`
- Limitacao documentada:
  - `PdfRenderer` nativo do Android nao oferece API para desbloquear PDF com senha.
  - A versao Android agora usa PDFBox-Android como fallback para tentar abrir PDFs protegidos com a senha informada.
  - Se a senha estiver incorreta, o app exibe erro amigavel.

PDF -> DOCX:

- Implementado como recurso experimental.
- Usa PDFBox-Android para extrair texto do PDF.
- Usa ML Kit Text Recognition como OCR experimental quando a pagina nao tem texto selecionavel.
- Gera um arquivo `.docx` local via SAF/CreateDocument.
- Preserva texto e paragrafos simples.
- Preserva quebras de pagina entre paginas extraidas.
- Infere titulos basicos quando o paragrafo e curto e esta em caixa alta.
- Detecta PDF sem texto mesmo apos OCR e informa erro amigavel.
- Nao reconstrói layout complexo, colunas ou tabelas visuais.
- OCR depende da qualidade da pagina e pode gerar texto incompleto em scans ruins.

Arquivo grande:

- Melhorias aplicadas:
  - processamento de uma pagina por vez;
  - fechamento de cada pagina apos renderizar;
  - reciclagem de bitmap;
  - limite de dimensao maxima do bitmap renderizado;
  - logs em Logcat;
  - tratamento especifico de memoria insuficiente.

Decisao Android MVP:

Incluir inicialmente:

- Imagem -> PDF.
- PDF -> Imagem.
- PDF protegido com senha para PDF -> Imagem.
- PDF -> DOCX experimental por extracao de texto.

Nao incluir inicialmente:

- DOCX -> PDF.

Motivo:

- Apache POI e pesado.
- DOCX -> PDF no Android e complexo.
- LibreOffice Headless nao funciona no Android.
- PDF -> DOCX tende a perder formatacao e exige parsing/reconstrucao avancados.

Status pendente:

- Rodar novo teste no emulador com PDF escaneado, PDF protegido, PDF -> DOCX, Imagem -> PDF multipagina e arquivo grande.
