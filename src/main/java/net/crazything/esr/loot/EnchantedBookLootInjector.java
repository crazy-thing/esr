package net.crazything.esr.loot;

import net.crazything.esr.EnchantingSystemReImagined;
import net.crazything.esr.config.EnchantingSystemReImaginedConfig;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;

public final class EnchantedBookLootInjector {

    private EnchantedBookLootInjector() {
    }

    private static final String CHEST_PATH_PREFIX = "chests/";

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            EnchantingSystemReImaginedConfig cfg = EnchantingSystemReImaginedConfig.get();
            if (!cfg.enchantedBookLootEnabled) {
                return;
            }

            if (!isChestLootTable(id)) {
                return;
            }

            List<LootPoolEntry> entries = buildEntries(cfg);
            if (entries.isEmpty()) {
                return;
            }
            float chance = (float) Math.max(0.0, Math.min(1.0, cfg.enchantedBookLootChance));
            LootPool.Builder poolBuilder = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(RandomChanceLootCondition.builder(chance))
                    .with(entries);

            tableBuilder.pool(poolBuilder);
            EnchantingSystemReImagined.LOGGER.debug(
                    "[Enchanting System ReImagined] Injected {} enchanted-book entries into {}",
                    entries.size(), id);
        });
    }

    private static boolean isChestLootTable(Identifier id) {
        return id.getPath().startsWith(CHEST_PATH_PREFIX);
    }

    private static List<LootPoolEntry> buildEntries(EnchantingSystemReImaginedConfig cfg) {
        List<LootPoolEntry> entries = new ArrayList<>();

        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            Identifier enchId = Registries.ENCHANTMENT.getId(enchantment);
            if (enchId == null)
                continue;
            if (enchantment.isCursed())
                continue;

            String key = enchId.toString();
            EnchantingSystemReImaginedConfig.EnchantmentCost cost = cfg.enchantmentCosts.get(key);

            if (cost == null) {
                cost = cfg.enchantmentCosts.get("default");
            }

            int weight = (cost != null) ? cost.lootWeight : 1;
            if (weight <= 0) {
                continue;
            }

            final Enchantment enc = enchantment;
            final int maxLevel = enc.getMaxLevel();
            LootPoolEntry entry = ItemEntry.builder(Items.ENCHANTED_BOOK)
                    .weight(weight)
                    .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(enc, ConstantLootNumberProvider.create(maxLevel)))
                    .build();

            entries.add(entry);
        }

        return entries;
    }
}
