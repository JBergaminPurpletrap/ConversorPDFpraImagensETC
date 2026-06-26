# Sugestoes 132 - Ajustes guardados para aplicar depois

Status: aplicado em 2026-06-16.

Pacote de melhorias:

1. Remover segredos do projeto Android:
   - Mover `android/keystore/document-converter-release.jks` para fora do projeto.
   - Remover senhas de `android/keystore/release-keystore.properties`.
   - Usar variaveis de ambiente ou arquivo local ignorado para assinatura release.

2. Criar ou atualizar `.gitignore`:
   - Ignorar `build/`, `target/`, `.gradle/`, APKs, AABs, JARs gerados.
   - Ignorar `local.properties`.
   - Ignorar keystore e propriedades de assinatura.

3. Ajustar PDF para Imagem:
   - Evitar sobrescrever `pagina_1`, `pagina_2` etc. de conversoes antigas.
   - Criar subpasta por conversao ou usar nome com data/hora.

4. Ajustar tela de conclusao:
   - Para PDF -> Imagem, deixar claro que "Abrir" abre apenas a primeira imagem ou trocar por abrir/compartilhar pasta/lista.

5. Splash Screen:
   - Se necessario, criar tela de boas-vindas composable apos o splash nativo para exibir o texto completo:
     "Converta arquivos localmente sem internet."

6. Testes automatizados:
   - Criar testes unitarios/instrumentados para conversao, cancelamento, PDF protegido e selecao multipla.

## Aplicado

- Criado `.gitignore`.
- Build Android passou a ler assinatura release de `../../.local/document-converter-android/release-keystore.properties` ou variaveis de ambiente.
- PDF para Imagem agora cria uma subpasta por conversao e nao sobrescreve imagens antigas.
- Tela de conclusao passa a mostrar `Abrir primeira` quando ha multiplas imagens.

## Pendente

- Criar testes automatizados reais.
- Se desejado, criar tela composable de boas-vindas apos splash nativo.
