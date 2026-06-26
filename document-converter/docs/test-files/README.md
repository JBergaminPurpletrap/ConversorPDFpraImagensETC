# Arquivos Oficiais de Teste

Esta pasta define o conjunto oficial de arquivos usados para validar o Document Converter.

Os arquivos binarios reais podem ser adicionados aqui quando forem criados manualmente, mas o projeto ja mantem o manifesto e o checklist para padronizar os testes.

## Estrutura recomendada

```text
docs/test-files/
├── README.md
├── manifest.json
├── manual-test-checklist.md
├── input/
│   ├── images/
│   ├── pdf/
│   └── docx/
└── expected/
    ├── android/
    └── desktop/
```

## Como usar

1. Coloque os arquivos de entrada em `input/`.
2. Execute as conversoes no Android e no desktop.
3. Salve resultados comparativos em `expected/` quando fizer sentido.
4. Atualize `manual-test-checklist.md` com data, plataforma e resultado.

## Casos obrigatorios

- Imagem para PDF com 1 imagem.
- Imagem para PDF com varias imagens.
- PDF para Imagem com PDF de 1 pagina.
- PDF para Imagem com PDF multipagina.
- PDF protegido por senha.
- PDF escaneado para DOCX com OCR.
- PDF com tabela simples para DOCX.
- PDF com imagem embutida para DOCX desktop.
- DOCX para PDF desktop com LibreOffice.
- Arquivo grande.

