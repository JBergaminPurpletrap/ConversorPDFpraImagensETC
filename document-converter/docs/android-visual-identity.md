# Document Converter Android - Identidade Visual

## Direcao visual

O aplicativo deve transmitir:

- Calma
- Organizacao
- Confiabilidade
- Simplicidade
- Natureza
- Produtividade

O visual evita cores agressivas, contrastes cansativos e tons muito saturados.

## Paleta

| Papel | Cor |
| --- | --- |
| Primary | `#556B2F` |
| Primary Container | `#DCE8C2` |
| Secondary | `#7A8F63` |
| Tertiary / Warning | `#B68D40` |
| Background | `#F4F3EF` |
| Surface | `#FFFFF6` |
| Surface Variant | `#EAE8E1` |
| Text Primary | `#2F3A2F` |
| Text Secondary | `#5A655A` |
| Success | `#4F7942` |
| Error | `#A65C4D` |

## Tema Material 3

O app usa um tema Material 3 completo em:

```text
android/app/src/main/java/com/documentconverter/android/ui/theme/AppTheme.kt
```

O tema define:

- `ColorScheme`
- `Typography`
- `Shapes`

As formas usam cantos arredondados moderados para uma aparencia moderna sem ficar informal demais.

## Componentes

### Cards

- Superficie clara.
- Elevacao leve.
- Bordas suaves.
- Conteudo com espacamento consistente.

### Botoes

- `Imagem para PDF`: verde musgo principal.
- `PDF para Imagem`: verde secundario.
- `Sobre`: cinza quente/neutro.
- Altura minima confortavel para toque.
- Elevacao leve.

### Feedback

- Status visual por estado.
- Barra de progresso com cor da identidade.
- Snackbar para sucesso e erro.

## Especificacao do icone do app

Ainda nao implementado.

Proposta recomendada:

- Base: documento com cantos arredondados.
- Elemento natural: folha estilizada sobreposta no canto inferior direito.
- Elemento de produtividade: linhas discretas no documento, sugerindo conteudo.
- Paleta:
  - Documento: `#556B2F`
  - Folha: `#7A8F63`
  - Fundo: `#F4F3EF`
  - Detalhes: `#FFFFF6`

Alternativa:

- Documento central.
- Duas setas circulares discretas representando conversao.
- Usar `#556B2F` e `#B68D40` com baixo contraste agressivo.

## Telas refinadas

- Tela inicial com marca visual, nome e descricao curta.
- Telas de conversao com area introdutoria, botoes claros e status persistente.
- Tela Sobre com versao, funcionalidades e limitacoes atuais.
