package net.crazything.esr.tick;

import net.crazything.esr.config.EnchantingSystemReImaginedConfig;
import net.crazything.esr.loot.GearItemDetection;
import net.crazything.esr.util.EnchantmentCapUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public final class SafetyNetTicker {

    private static int tickCounter = 0;

    private SafetyNetTicker() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(SafetyNetTicker::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        EnchantingSystemReImaginedConfig config = EnchantingSystemReImaginedConfig.get();
        int interval = Math.max(1, config.safetyNetIntervalTicks);

        tickCounter++;
        if (tickCounter < interval) {
            return;
        }
        tickCounter = 0;

        for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sweepInventory(player.getInventory());
        }

        for (ServerWorld world : server.getWorlds()) {
            world.getEntitiesByType(net.minecraft.entity.EntityType.ITEM, itemEntity -> true)
                    .forEach(SafetyNetTicker::sweepItemEntity);
        }
    }

    private static void sweepInventory(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                if (GearItemDetection.isGearItem(stack.getItem())
                        || GearItemDetection.isEnchantedBook(stack.getItem())) {
                    stack.getOrCreateNbt().putBoolean("esr$PlayerOwned", true);
                }
                maybeFix(stack);
            }
        }
    }

    private static void sweepItemEntity(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getStack();

        boolean isPlayerOwned = stack.hasNbt() && stack.getNbt().getBoolean("esr$PlayerOwned");

        EnchantingSystemReImaginedConfig config = EnchantingSystemReImaginedConfig.get();
        if (!isPlayerOwned
                && config.bestEffortLootStripOutsideLootTables
                && GearItemDetection.isGearItem(stack.getItem())
                && itemEntity.getOwner() == null
                && itemEntity.getItemAge() <= 5) {
            stack.removeSubNbt("Enchantments");
            stack.removeSubNbt("StoredEnchantments");
            itemEntity.setStack(stack);
            return;
        }

        maybeFix(stack);
    }

    private static void maybeFix(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!EnchantmentCapUtil.isCompliant(stack)) {
            EnchantmentCapUtil.enforceSingleEnchantment(stack);
        }
    }
}
