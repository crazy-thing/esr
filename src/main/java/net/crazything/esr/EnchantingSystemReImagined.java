package net.crazything.esr;

import net.crazything.esr.config.EnchantingSystemReImaginedConfig;
import net.crazything.esr.loot.EnchantedBookLootInjector;
import net.crazything.esr.loot.LootDeenchantHandler;
import net.crazything.esr.loot.StripGearEnchantmentsLootFunction;
import net.crazything.esr.tick.SafetyNetTicker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantingSystemReImagined implements ModInitializer {

    public static final String MOD_ID = "esr";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Enchanting System ReImagined] Initializing - loot de-enchanting, single-enchant cap, "
                + "chiseled-bookshelf guaranteed enchanting, and modded enchanted-book loot injection");

        EnchantingSystemReImaginedConfig.get();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info(
                    "[Enchanting System ReImagined] Server started — rescanning enchantment registry for new modded enchantments.");
            EnchantingSystemReImaginedConfig.get().updateFromRegistry();
        });

        StripGearEnchantmentsLootFunction.register(new Identifier(MOD_ID, "strip_gear_enchantments"));
        LootDeenchantHandler.register();
        EnchantedBookLootInjector.register();
        SafetyNetTicker.register();

    }
}
