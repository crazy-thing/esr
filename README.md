<p align="center">
  <img src="esrheader.png" alt="Enchanting System ReImagined (ESR)" width="800/>
</p>

# Enchanting System ReImagined (ESR)

ESR is a Minecraft Fabric mod for 1.20.1 that replaces vanilla enchanting RNG with a system built around deliberate choice and limits.

Instead of rolling the table for hours or stacking a dozen passive buffs on one piece of armor, items are limited to a single, max-level enchantment, and options can be guaranteed by placing enchanted books in chiseled bookshelves.

---

## Features

### 1. One Enchantment Limit

Every item can have at most one enchantment.
* The enchantment is always applied at its maximum level (placing Sharpness on a sword would give you Sharpness V stats).
* Curses have a chance to also be applied to an item along side the desired enchantment if the enchantment fails.
* Anvils are used only for repairs; combining items or books to stack enchantments is disabled.

### 2. Chiseled Bookshelves Guarantee Enchantments

* Place enchanted books in chiseled bookshelves around the enchanting table to guarantee those exact options on the table.
* Enchanted books placed in chiseled bookselves are consumed when used to enchant an item (this can be disabled in the config).
* Regular bookshelves nearby give XP and lapis discounts on the enchanting.
* The table interface is updated to show readable enchantment names (Sharpness) instead of Galactic runes, and includes scrollbar support when you have many shelves.

### 3. De-enchanted Loot

* Armor, weapons, and tools generated in loot chests spawn without enchantments.
* Enchanted books still spawn in chests, but conform to the single-enchantment cap.
* Modded and vanilla enchanted books are injected into chest loot pools with configurable weights so all enchantments have a fair spawn rate.

---

## Config (enchanting-system-reimagined.json)

You can edit these options directly in the JSON file or in-game via Mod Menu:

* **allowBookEnchanting** (default: false): Enables or disables putting plain books on the enchanting table.
* **consumeEnchantedBook** (default: true): If false, books inside chiseled bookshelves are not consumed when used.
* **curseFailureChance** (default: 0.15): Chance that a random curse is added alongside your chosen enchantment.
* **maxBookshelfDiscountCount** (default: 15): Max regular bookshelves counted for lapis/XP discounts.

---

## Building

Requires JDK 17. Run:

```bash
./gradlew build
```

The compiled jar file will be placed in `build/libs/`.
