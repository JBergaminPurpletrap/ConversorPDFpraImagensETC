Imagem -> PDF: funcionou.
PDF -> Imagem: funcionou parcialmente no teste inicial.
DOCX simples: nao funcionou.
PDF protegido: nao abriu por exigir senha.
Arquivo grande: nao funcionou.

Erro principal encontrado:

- PDF -> Imagem convertia apenas a primeira pagina do PDF.

Correcao aplicada na POC:

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

- PDF protegido por senha passa a retornar mensagem amigavel:
  - `Este PDF esta protegido por senha. A versao Android ainda nao suporta PDFs protegidos.`

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

Nao incluir inicialmente:

- DOCX -> PDF.
- PDF -> DOCX.

Motivo:

- Apache POI e pesado.
- DOCX -> PDF no Android e complexo.
- LibreOffice Headless nao funciona no Android.
- PDF -> DOCX tende a perder formatacao e exige parsing/reconstrucao avancados.

Status pendente:

- Rodar novo teste no emulador com PDF multipagina, PDF protegido e arquivo grande.
