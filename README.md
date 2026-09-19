# QuietSort

[![build](https://github.com/LFARRUDA188/QuietSort/actions/workflows/build.yml/badge.svg)](https://github.com/LFARRUDA188/QuietSort/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Português:** [README.pt-BR.md](README.pt-BR.md)

Chests and backpacks that sort themselves when you close them. No GUI, no hotkeys, no commands to memorise — players just play, and their storage stays tidy.

## Why another sorting plugin?

Most sorting plugins ask the player to *do* something: middle-click, shift-right-click, double-right-click an empty slot, or type a command. That works badly for players on Bedrock/mobile, where those gestures often don't translate through Geyser at all.

QuietSort has no trigger to learn. You close the chest, it's sorted.

| | QuietSort |
|---|---|
| Player action required | none |
| GUI / menus | none |
| Works on Bedrock (Geyser) | yes — closing a chest is the only gesture needed |
| Hotbar reordered | **never** |
| Armor / off-hand touched | never |

## What it sorts

- **Chests, barrels, shulker boxes and ender chests** — sorted when closed.
- **The player backpack (slots 9–35)** — sorted when the player closes their own inventory.
- Matching stacks are merged up to their max stack size.
- Order: blocks → tools & weapons → armor → food → potions → everything else, alphabetically within each group.

## Not losing your items

Inventory plugins are the ones that can genuinely ruin a world, so the design starts there:

- Sorting happens on a **copy**, never on the live inventory.
- Before writing anything back, QuietSort **compares the total item count** against what was there before. If the numbers don't match, it writes nothing and leaves the container exactly as it was.
- If a stack is larger than the item's own max size (some plugins create these), and normalising it wouldn't fit, QuietSort refuses rather than dropping the overflow.
- Containers **open by someone else** are skipped, to avoid desync between viewers.
- **Plugin menus** (BedrockGUI, shop GUIs, etc.) are identified by their inventory holder and ignored on purpose.
- Sorting runs on the **next tick** after close, not during the close event, which is what prevents client desync — including for Geyser players.

The sorting logic is deliberately isolated from the server API so it can be tested directly: `gradle build` runs 20 checks including a fuzz test over 2000 randomly generated inventories, asserting that no item is ever lost or duplicated.

## Install

1. Download `QuietSort-x.y.z.jar` from [Releases](https://github.com/LFARRUDA188/QuietSort/releases).
2. Drop it into your server's `plugins/` folder.
3. Restart the server.

There is nothing to configure to get the default behaviour.

> **Remove other sorting plugins first.** Two plugins sorting the same chest will fight each other.

## Configuration

`plugins/QuietSort/config.yml`:

```yaml
# Sort chests, barrels and shulker boxes when closed
sort-containers: true

# Sort the player backpack (slots 9-35) when the player closes their own inventory.
# The HOTBAR (slots 0-8), ARMOR and OFF-HAND are NEVER touched.
sort-player-inventory: true

# Also sort the ender chest
sort-ender-chest: true

# Also sort the backpack when closing a container (default: no)
sort-player-inventory-on-container-close: false
```

## Commands and permissions

| Command | What it does |
|---|---|
| `/quietsort` (`/qsort`) | Toggles automatic sorting **for the player who typed it** — for people who organise their own chests and don't want help |
| `/quietsort reload` | Reloads the config (requires `quietsort.admin`) |

| Permission | Default | Meaning |
|---|---|---|
| `quietsort.use` | everyone | Automatic sorting applies to this player |
| `quietsort.admin` | op | May reload the configuration |

## Compatibility

- **Server:** Paper and forks (Purpur, Folia-compatible builds not verified). Spigot is untested.
- **Verified on:** Purpur 26.2 with Java 25.
- **Expected to work on:** Paper 1.21 and newer, including the 26.x series. The plugin only uses long-stable Bukkit API, so it should survive version bumps without recompiling — but only 26.2 has been tested in production.
- **Bedrock:** works through Geyser/Floodgate. No client mod required for anyone.

## Building from source

```bash
git clone https://github.com/LFARRUDA188/QuietSort.git
cd QuietSort
gradle build
```

The jar lands in `build/libs/`. `gradle build` also runs the sorting tests.

To run only the tests:

```bash
gradle logicTestRun
```

## License

MIT — see [LICENSE](LICENSE).
