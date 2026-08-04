<p align="center">
  <img src="ESR.png" alt="ESR Logo" width="250"/>
</p>

# Enchanting System ReImagined (ESR)

ESR is a Minecraft Fabric mod for 1.20.1 that replaces vanilla enchanting RNG with a system built around deliberate choice and limits.

Instead of rolling the table for hours or stacking a dozen passive buffs on one piece of armor, items are limited to a single, max-level enchantment, and options can be guaranteed by placing enchanted books in chiseled bookshelves.

---

## Features

### 1. One Enchantment Limit
* Every item can hold **at most one** regular enchantment.
* The enchantment is always applied at its **maximum level** (e.g., placing Sharpness on a sword gives you Sharpness V immediately).
* Curses are preserved and also set to their max level.
* Anvils are used only for repairs; combining items or books to stack enchantments is disabled.

### 2. Chiseled Bookshelves Guarantee Enchantments
* Place enchanted books in chiseled bookshelves around the enchanting table to guarantee those exact options on the table.
* Using a bookshelf option consumes the book (this can be disabled in the config).
* Regular bookshelves nearby still give XP and lapis discounts on the costs.
* The table interface is updated to show readable enchantment names (e.g., "Sharpness V") instead of Galactic runes, and includes scrollbar support when you have many shelves.

### 3. De-enchanted Loot
* Armor, weapons, and tools generated in loot chests spawn without enchantments.
* Enchanted books still spawn in chests, but conform to the single-enchantment cap.
* Modded and vanilla enchanted books are injected into chest loot pools with configurable weights so all enchantments have a fair spawn rate.

---

## Config (`config/enchanting-system-reimagined.json`)

You can edit these options directly in the JSON file or in-game via Mod Menu:

* `allowBookEnchanting` (default: `false`): Enables or disables putting plain books on the enchanting table.
* `consumeEnchantedBook` (default: `true`): If false, books inside chiseled bookshelves are not consumed when used.
* `curseFailureChance` (default: `0.15`): Chance that a random curse is added alongside your chosen enchantment.
* `maxBookshelfDiscountCount` (default: `15`): Max regular bookshelves counted for lapis/XP discounts.

### Example JSON Config
```json
{
  "safetyNetIntervalTicks": 20,
  "bestEffortLootStripOutsideLootTables": true,
  "allowBookEnchanting": false,
  "guaranteedSlotOrdering": "NEAREST_FIRST",
  "maxBookshelfDiscountCount": 15,
  "lapisDiscountPerBookshelf": 0.03,
  "xpDiscountPerBookshelf": 0.03,
  "enchantedBookLootEnabled": true,
  "enchantedBookLootChance": 0.05,
  "curseFailureChance": 0.15,
  "consumeEnchantedBook": true,
  "enchantmentCosts": {
    "default": {
      "lapis": 10,
      "xp": 5,
      "isProtected": false,
      "lootWeight": 1
    },
    "minecraft:unbreaking": {
      "lapis": 10,
      "xp": 5,
      "isProtected": false,
      "lootWeight": 1
    },
    "minecraft:efficiency": {
      "lapis": 12,
      "xp": 7,
      "isProtected": true,
      "lootWeight": 2
    },
    "some_modded_enchantment:fortune": {
      "lapis": 15,
      "xp": 8,
      "isProtected": false,
      "lootWeight": 1
    }
  }
}
```

* `isProtected`: If `true`, the config UI's "Sync Costs" or "Sync Weights" buttons will not overwrite this enchantment's values.
* `lootWeight`: Set to `0` to disable the book from spawning in chest loot pools entirely. Higher numbers make it spawn proportionally more often.

---

## Building

Requires JDK 17. Run:

```bash
./gradlew build
```

The compiled jar file will be placed in `build/libs/`.
