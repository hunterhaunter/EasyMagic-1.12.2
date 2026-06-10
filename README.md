# Easy Magic (1.12.2 Backport)

Backport of [Easy Magic](https://www.curseforge.com/minecraft/mc-mods/easy-magic) by Fuzs to Minecraft 1.12.2.

Keeps your item and lapis in the enchanting table when you close the GUI, shows floating item renders around the block, and adds a reroll button for enchantment options at a small lapis + XP cost. Also includes configurable enchantment previews, bookshelf power requirements, and lets you place non-solid blocks between bookshelves and the table without affecting enchantment level.

## Requirements

- Minecraft 1.12.2
- Minecraft Forge 14.23.5.2860+
- [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter) 9.1+

## Mod Compatibility

Patches vanilla classes via Mixin only — no block or container replacement, so other
enchanting mods keep working. Tested with:

- [Reagenchant](https://www.curseforge.com/minecraft/mc-mods/reagenchant) — item retention,
  floating item render, reroll button, and enchantment hints all work on the reagent table
- Enchantment Control
- Enchanting Plus

## Building

`./gradlew build` (RetroFuturaGradle). The Reagenchant compat mixins compile against the
real jars, so building requires `Reagenchant-1.12.2-1.2.1.jar`, `LibraryEx-1.12.2-1.2.2.jar`,
and the night-config core/json 3.6.0 jars in `libs/` 
