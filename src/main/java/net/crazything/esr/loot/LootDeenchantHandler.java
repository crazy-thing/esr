package net.crazything.esr.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;

public final class LootDeenchantHandler {

    private LootDeenchantHandler() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            tableBuilder.apply(() -> StripGearEnchantmentsLootFunction.INSTANCE);
        });
    }
}
