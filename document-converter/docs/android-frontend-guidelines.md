# Guia de Front-end Android - Document Converter

## Objetivo

Este documento define como a parte visual Android deve ser organizada, mantida e evoluida.

O foco e garantir:

- Interface consistente.
- Codigo limpo.
- Componentes reutilizaveis.
- Tema centralizado.
- Boa acessibilidade.
- Facilidade para adicionar novas telas no futuro.

## Estrutura Recomendada

Toda a interface Android deve ficar dentro de:

```text
android/app/src/main/java/com/documentconverter/android/ui/
```

Estrutura padrao:

```text
ui/
├── components/
├── model/
├── navigation/
├── screens/
│   ├── about/
│   ├── converter/
│   ├── home/
│   └── result/
├── state/
└── theme/
    ├── Color.kt
    ├── Dimens.kt
    ├── Shape.kt
    ├── Theme.kt
    └── Type.kt
```

## Responsabilidades por Pasta

### `theme/`

Centraliza toda a identidade visual.

Regras:

- Cores ficam em `Color.kt`.
- Tipografia fica em `Type.kt`.
- Arredondamentos ficam em `Shape.kt`.
- Espacamentos e tamanhos ficam em `Dimens.kt`.
- O tema Material 3 final fica em `Theme.kt`.
- Evitar cores, tamanhos e fontes soltos diretamente nas telas.

### `components/`

Contem componentes reutilizaveis.

Exemplos:

- `AppButton.kt`
- `AppCard.kt`
- `AppTopBar.kt`
- `DocumentList.kt`
- `StatusPanel.kt`
- `EmptyState.kt`
- `LoadingState.kt`

Regras:

- Componentes devem receber apenas os dados necessarios por parametro.
- Componentes nao devem conhecer regras de negocio.
- Componentes nao devem chamar conversores.
- Componentes nao devem acessar `ContentResolver`, arquivos ou Android SAF.
- Componentes devem ser pequenos e reutilizaveis.

### `screens/`

Contem as telas completas do app.

Regras:

- Cada tela deve ficar em uma subpasta por dominio.
- Telas devem orquestrar componentes visuais.
- Telas podem observar estado do ViewModel.
- Telas nao devem implementar logica pesada.
- Telas nao devem conter logica de conversao.

Exemplos:

```text
screens/home/HomeScreen.kt
screens/converter/ImageToPdfScreen.kt
screens/converter/PdfToImageScreen.kt
screens/about/AboutScreen.kt
screens/result/CompletionPanel.kt
```

### `navigation/`

Contem navegacao visual.

Regras:

- `MainActivity` deve apenas hospedar o app.
- Navegacao entre telas deve ficar em `AppNavigation`.
- Novas telas devem ser registradas no enum `AppScreen`.
- O botao voltar do Android deve voltar para a tela anterior.
- Na tela inicial, o usuario deve pressionar voltar duas vezes para sair.

### `state/`

Contem estados visuais da UI.

Exemplos:

- `ConversionState`
- `ConversionStatus`

Regras:

- Estados visuais devem ser simples.
- Estados nao devem depender de Compose quando nao for necessario.
- Estados devem ser reaproveitaveis entre telas.

### `model/`

Contem modelos especificos da interface.

Exemplos:

- `SelectedDocument`
- `CompletedConversion`

Regras:

- Modelos de UI devem representar apenas dados exibidos ou usados pela tela.
- Modelos de UI nao devem conter regras de conversao.

## Regras de Tema Visual

### Cores

O app deve manter identidade natural e agradavel:

- Fundo principal em verde escuro.
- Cards em verde escuro levemente mais claro.
- Botoes em tons claros de verde/salvia.
- Texto comum em tons claros.
- Texto de botoes em preto.
- Erros em cor visivel, mas sem vermelho agressivo.

Regras obrigatorias:

- Usar `AppColors` para cores.
- Usar `MaterialTheme.colorScheme` quando possivel.
- Nao espalhar `Color(0x...)` em telas.
- Texto dentro de botao claro deve usar `AppColors.ButtonText`.

### Tipografia

Regras:

- Usar `MaterialTheme.typography`.
- Evitar definir `fontSize` diretamente em telas.
- Titulos principais usam `headlineMedium` ou `headlineSmall`.
- Titulos de cards usam `titleMedium` ou `titleSmall`.
- Textos auxiliares usam `bodyMedium` ou `bodySmall`.

### Espacamentos

Regras:

- Usar `AppDimens`.
- Evitar valores como `16.dp`, `8.dp`, `50.dp` diretamente nas telas.
- Telas devem usar `ScreenBody`.
- Cards devem usar `AppOutlinedCard` ou `AppElevatedCard`.

## Regras de Componentes

### Botoes

Usar:

- `AppPrimaryButton` para acoes principais.
- `AppSecondaryButton` para acoes secundarias.
- `AppFullWidthSecondaryButton` para acoes secundarias em largura total.

Regras:

- Texto dos botoes sempre preto.
- Altura minima deve vir de `AppDimens`.
- Botoes devem ter nomes curtos.
- Evitar duplicar codigo de `ElevatedButton` diretamente nas telas.

### Cards

Usar:

- `AppOutlinedCard`
- `AppElevatedCard`

Regras:

- Cards devem usar padding padronizado.
- Nao colocar cards dentro de cards sem necessidade.
- Cards devem agrupar informacoes relacionadas.

### Feedback Visual

Usar:

- `StatusPanel` para progresso, erro e sucesso.
- `CompletionPanel` para conversao concluida.
- `SnackbarHostState` para mensagens temporarias.
- `EmptyState` quando nao houver conteudo.
- `LoadingState` para carregamentos simples.

## Regras de Telas

Cada tela deve seguir esta estrutura:

1. Descricao curta do fluxo.
2. Acoes principais.
3. Conteudo selecionado ou resultado.
4. Painel de status.
5. Painel de conclusao quando aplicavel.

Exemplo:

```kotlin
@Composable
fun MinhaTela(viewModel: MeuViewModel) {
    ScreenBody {
        AppOutlinedCard {
            Text(...)
        }

        AppPrimaryButton(...)

        StatusPanel(state = viewModel.state)
    }
}
```

## Regras de ViewModel e UI

Regras:

- ViewModel controla estado.
- Tela exibe estado.
- Tela dispara eventos simples para o ViewModel.
- Conversores nao devem ser chamados diretamente por componentes visuais.
- UI nao deve saber detalhes internos de conversao.

Permitido na tela:

- Abrir seletor SAF.
- Chamar metodo do ViewModel.
- Exibir Snackbar.
- Chamar helper visual de compartilhamento.

Nao permitido na tela:

- Renderizar PDF.
- Criar PDF.
- Manipular bitmap.
- Abrir streams manualmente.
- Validar regra complexa de arquivo.

## Navegacao e Back

Regras:

- Toda nova tela deve entrar em `AppScreen`.
- O titulo da tela deve ser definido pela navegacao.
- Botao voltar da top bar deve voltar para Home.
- Botao voltar do Android deve voltar para Home quando estiver em outra tela.
- Na Home, dois backs seguidos fecham o app.

## Acessibilidade

Regras:

- Componentes clicaveis devem ter altura minima confortavel.
- Texto deve ter contraste suficiente.
- Evitar textos longos em botoes.
- Nao depender apenas de cor para indicar estado.
- Mensagens de erro devem ser claras.

## Nomenclatura

### Componentes

Usar prefixo `App` para componentes base:

- `AppPrimaryButton`
- `AppOutlinedCard`
- `AppTopBar`

Componentes de dominio podem ter nome especifico:

- `DocumentList`
- `StatusPanel`
- `CompletionPanel`

### Telas

Telas devem terminar com `Screen`:

- `HomeScreen`
- `ImageToPdfScreen`
- `PdfToImageScreen`
- `AboutScreen`

### Estados

Estados devem ter nomes claros:

- `ConversionState`
- `ConversionStatus`
- `CompletedConversion`

## Checklist Antes de Finalizar Mudancas de UI

- Build debug Android compila.
- Nenhuma regra de conversao foi alterada sem necessidade.
- Novos valores visuais foram colocados em `theme`.
- Componentes repetidos foram extraidos para `components`.
- Nova tela foi registrada em `navigation`.
- Textos de botoes estao pretos quando o fundo do botao e claro.
- A navegacao por back continua funcionando.
- Documentacao foi atualizada quando houver novo padrao.

## Comando de Validacao

```powershell
cd "C:\Projetinhos\Projeto conversor pdf\document-converter\android"
& "C:\Projetinhos\Projeto conversor pdf\.tools\gradle-9.5.1\bin\gradle.bat" :app:assembleDebug
```
