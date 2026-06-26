# Document Converter Android - Checklist de Qualidade

## Testes obrigatorios

| Cenario | Resultado esperado | Status |
| --- | --- | --- |
| Imagem para PDF com 1 imagem | PDF criado com sucesso | Pendente |
| Imagem para PDF com varias imagens | PDF multipagina criado | Pendente |
| PDF para Imagem com 1 pagina em JPEG | `pagina_1.jpg` criada | Pendente |
| PDF para Imagem com 1 pagina em PNG | `pagina_1.png` criada | Pendente |
| PDF multipagina | Todas as paginas geradas | Pendente |
| Arquivo grande | Conversao conclui sem travar | Pendente |
| Cancelar seletor de imagens | App continua estavel | Pendente |
| Cancelar seletor de PDF | App continua estavel | Pendente |
| Cancelar local de salvamento | App continua estavel | Pendente |
| PDF protegido | Mensagem amigavel exibida | Pendente |
| Compartilhar PDF gerado | Android abre seletor de compartilhamento | Pendente |
| Compartilhar imagens geradas | Android abre seletor de compartilhamento | Pendente |
| Abrir arquivo gerado | Android oferece app compativel | Pendente |

## Logs

Verificar se o app registra:

- `START`
- `SUCCESS`
- `ERROR`
- `elapsedMs`
- quantidade de saidas

Local interno do app:

```text
files/logs/conversion-log.txt
```

## Comandos de build

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android"
gradle :app:assembleDebug
gradle :app:assembleRelease
gradle :app:bundleRelease
```

## Resultado da Fase 15

Atualizar este documento apos novo teste manual no emulador/dispositivo.
