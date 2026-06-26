# Checklist Manual de Testes

Use este checklist antes de gerar um APK/EXE para distribuicao.

## Android

- [ ] Instalar `app-debug.apk` em emulador ou aparelho real.
- [ ] Converter 1 imagem para PDF.
- [ ] Converter varias imagens para PDF.
- [ ] Converter PDF de 1 pagina para JPEG.
- [ ] Converter PDF multipagina para JPEG.
- [ ] Converter PDF multipagina para PNG.
- [ ] Converter PDF protegido com senha correta.
- [ ] Converter PDF protegido com senha incorreta e validar erro amigavel.
- [ ] Converter PDF escaneado para DOCX com OCR ligado.
- [ ] Converter PDF escaneado para DOCX com OCR desligado.
- [ ] Converter PDF com tabela simples para DOCX.
- [ ] Testar arquivo grande.

## Desktop

- [ ] Rodar `.\mvnw.cmd -pl desktop javafx:run`.
- [ ] Converter 1 imagem para PDF.
- [ ] Converter varias imagens para PDF.
- [ ] Converter PDF multipagina para imagem.
- [ ] Converter PDF protegido para imagem com senha correta.
- [ ] Converter PDF protegido para DOCX com senha correta.
- [ ] Converter PDF escaneado para DOCX com Tesseract configurado.
- [ ] Converter PDF com tabela simples para DOCX.
- [ ] Converter PDF com imagem embutida para DOCX.
- [ ] Converter DOCX para PDF com LibreOffice configurado.
- [ ] Gerar instalador `.exe`.
- [ ] Assinar instalador quando houver certificado.

## Registro de execucao

```text
Data:
Versao:
Ambiente:
Android APK:
Windows EXE:
Responsavel:
Resultado geral:
Observacoes:
```

