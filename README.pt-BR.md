# QuietSort

[![build](https://github.com/LFARRUDA188/QuietSort/actions/workflows/build.yml/badge.svg)](https://github.com/LFARRUDA188/QuietSort/actions/workflows/build.yml)
[![Licença: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**English:** [README.md](README.md)

Baús e mochilas que se organizam sozinhos ao fechar. Sem menu, sem atalho, sem comando pra decorar — o jogador só joga, e o armazenamento se mantém arrumado.

## Por que mais um plugin de organização?

Quase todos os plugins de organização exigem que o jogador *faça* alguma coisa: clique do meio, shift + clique direito, duplo clique direito num slot vazio, ou digitar um comando. Isso funciona mal para quem joga no Bedrock/celular, onde esses gestos muitas vezes nem chegam a ser traduzidos pelo Geyser.

O QuietSort não tem gesto pra aprender. Você fecha o baú, ele está organizado.

| | QuietSort |
|---|---|
| Ação necessária do jogador | nenhuma |
| Menu / interface | nenhum |
| Funciona no Bedrock (Geyser) | sim — fechar o baú é o único gesto necessário |
| Mexe na hotbar | **nunca** |
| Mexe na armadura / mão secundária | nunca |

## O que ele organiza

- **Baús, barris, shulkers e ender chest** — organizados ao fechar.
- **A mochila do jogador (slots 9–35)** — organizada quando o jogador fecha o próprio inventário.
- Pilhas iguais são juntadas até o limite de cada item.
- Ordem: blocos → ferramentas e armas → armaduras → comida → poções → o resto, em ordem alfabética dentro de cada grupo.

## Por que seus itens não somem

Plugin de inventário é o tipo de plugin que estraga um mundo de verdade, então o projeto começa por aí:

- A ordenação acontece numa **cópia**, nunca no inventário ao vivo.
- Antes de gravar qualquer coisa, o QuietSort **confere a soma total de itens** contra o que havia antes. Se os números não baterem, ele não grava nada e deixa o baú exatamente como estava.
- Se uma pilha for maior que o limite do próprio item (alguns plugins criam isso), e ao normalizar não couber, o QuietSort recusa em vez de descartar o excedente.
- Baús **abertos por outra pessoa** são ignorados, pra evitar dessincronia entre quem está vendo.
- **Menus de plugin** (BedrockGUI, GUI de loja, etc.) são identificados pelo dono do inventário e ignorados de propósito.
- A ordenação roda no **tick seguinte** ao fechar, não durante o evento de fechamento — é isso que evita dessincronia com o cliente, inclusive para quem joga via Geyser.

A lógica de ordenação foi deliberadamente isolada da API do servidor para poder ser testada direto: `gradle build` roda 20 verificações, incluindo um teste de estresse sobre 2000 inventários gerados aleatoriamente, garantindo que nenhum item é perdido ou duplicado.

## Instalação

1. Baixe o `QuietSort-x.y.z.jar` em [Releases](https://github.com/LFARRUDA188/QuietSort/releases).
2. Coloque na pasta `plugins/` do servidor.
3. Reinicie o servidor.

Não precisa configurar nada para o comportamento padrão funcionar.

> **Remova outros plugins de organização antes.** Dois plugins organizando o mesmo baú vão brigar entre si.

## Configuração

`plugins/QuietSort/config.yml`:

```yaml
# Organiza baús, barris e shulkers ao fechar
sort-containers: true

# Organiza a mochila (slots 9-35) ao fechar o próprio inventário.
# A HOTBAR (slots 0-8), a ARMADURA e a MÃO SECUNDÁRIA NUNCA são tocadas.
sort-player-inventory: true

# Organiza também o ender chest
sort-ender-chest: true

# Organiza a mochila também ao fechar um baú (padrão: não)
sort-player-inventory-on-container-close: false
```

## Comandos e permissões

| Comando | O que faz |
|---|---|
| `/quietsort` (`/qsort`) | Liga/desliga a organização automática **só para quem digitou** — para quem organiza os próprios baús e não quer ajuda |
| `/quietsort reload` | Recarrega a configuração (precisa de `quietsort.admin`) |

| Permissão | Padrão | Significado |
|---|---|---|
| `quietsort.use` | todos | A organização automática vale para esse jogador |
| `quietsort.admin` | op | Pode recarregar a configuração |

## Compatibilidade

- **Servidor:** Paper e derivados (Purpur; builds compatíveis com Folia não foram verificadas). Spigot não foi testado.
- **Testado em:** Purpur 26.2 com Java 25.
- **Deve funcionar em:** Paper 1.21 ou mais novo, incluindo a série 26.x. O plugin usa apenas API antiga e estável do Bukkit, então deve atravessar atualizações sem precisar recompilar — mas só a 26.2 foi testada em produção.
- **Bedrock:** funciona via Geyser/Floodgate. Nenhum mod de cliente é necessário.

## Compilando do código-fonte

```bash
git clone https://github.com/LFARRUDA188/QuietSort.git
cd QuietSort
gradle build
```

O jar aparece em `build/libs/`. O `gradle build` também roda os testes de ordenação.

Para rodar só os testes:

```bash
gradle logicTestRun
```

## Licença

MIT — veja [LICENSE](LICENSE).
