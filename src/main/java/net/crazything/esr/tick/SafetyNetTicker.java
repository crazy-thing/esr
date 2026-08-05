package net.crazything.esr.tick;

import net.crazything.esr.config.EnchantingSystemReImaginedConfig;
import net.crazything.esr.loot.GearItemDetection;
import net.crazything.esr.util.EnchantmentCapUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sweepPlayer(player);
        }

        for (ServerWorld world : server.getWorlds()) {
            world.getEntitiesByType(net.minecraft.entity.EntityType.ITEM, itemEntity -> true)
                    .forEach(SafetyNetTicker::sweepItemEntity);
        }
    }

    private static void sweepPlayer(ServerPlayerEntity player) {
        boolean modified = sweepInventory(player.getInventory());

        if (player.currentScreenHandler != null && player.currentScreenHandler != player.playerScreenHandler) {
            for (int i = 0; i < player.currentScreenHandler.slots.size(); i++) {
                ItemStack stack = player.currentScreenHandler.slots.get(i).getStack();
                if (!stack.isEmpty()) {
                    if (maybeFix(stack)) {
                        modified = true;
                    }
                }
            }
        }

        if (modified) {
            player.playerScreenHandler.sendContentUpdates();
            if (player.currentScreenHandler != null) {
                player.currentScreenHandler.sendContentUpdates();
            }
        }
    }

    private static boolean sweepInventory(Inventory inventory) {
        boolean modified = false;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                if (GearItemDetection.isGearItem(stack) || GearItemDetection.isEnchantedBook(stack)) {
                    stack.getOrCreateNbt().putBoolean("esr$PlayerOwned", true);
                }
                if (maybeFix(stack)) {
                    modified = true;
                }
            }
        }
        return modified;
    }

    private static void sweepItemEntity(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getStack();
        if (stack.isEmpty()) {
            return;
        }

        boolean isPlayerOwned = stack.hasNbt() && stack.getNbt().getBoolean("esr$PlayerOwned");

        EnchantingSystemReImaginedConfig config = EnchantingSystemReImaginedConfig.get();
        if (!isPlayerOwned
                && config.bestEffortLootStripOutsideLootTables
                && GearItemDetection.isGearItem(stack)
                && itemEntity.getOwner() == null) {
            stack.removeSubNbt("Enchantments");
            stack.removeSubNbt("StoredEnchantments");
            itemEntity.setStack(stack);
            return;
        }

        if (maybeFix(stack)) {
            itemEntity.setStack(stack);
        }
    }

    private static boolean maybeFix(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!EnchantmentCapUtil.isCompliant(stack)) {
            EnchantmentCapUtil.enforceSingleEnchantment(stack);
            return true;
        }
        return false;
    }
}

