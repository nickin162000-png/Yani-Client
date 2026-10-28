# Yani Client (Forge 1.8.9)

Mudancas em relacao ao Gangue Da Welcia 5.3:
- Nome, modid (yaniclient), pacote (com.yani.client) e config (config/yaniclient.properties) trocados.
- Logo novo e original: src/main/resources/assets/yaniclient/textures/logo.png (branco, o client pinta com a cor do tema).
- Novo menu de mods (ModMenu.java, tecla Right Shift): grade de cards com OPCOES e ATIVADO/DESATIVADO,
  filtros (TODOS/NOVOS/HUD/COMBATE/UTILIDADES/INTERFACE), busca, perfis (Padrao, UHC, Arena PvP + salvar os seus)
  e botao EDITAR HUD. As abas MIRA / TAMANHO / AJUSTES abrem as telas de configuracao que ja existiam.
- Logo na tela inicial e no menu de pausa.
- As opcoes (modulos) sao exatamente as mesmas do original.

Observacao: o codigo usa nomes SRG (func_XXXX), igual ao original. Compile com o mesmo metodo usado no original.

## Como gerar o .jar pelo GitHub
1. Crie um repositorio no GitHub e envie todo o conteudo desta pasta (incluindo a pasta `.github`).
2. Abra a aba **Actions** > **Build Yani Client** > **Run workflow** (ele tambem roda sozinho a cada push em `main`).
3. Quando terminar (verde), abra a execucao e baixe o arquivo em **Artifacts > YaniClient**. Dentro esta o `YaniClient-1.0.jar`.
4. Coloque o .jar na pasta `mods` do Minecraft 1.8.9 com Forge 11.15.1.2318.
