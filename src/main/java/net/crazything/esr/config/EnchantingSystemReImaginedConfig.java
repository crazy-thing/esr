package net.crazything.esr.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.crazything.esr.EnchantingSystemReImagined;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EnchantingSystemReImaginedConfig {

    public int safetyNetIntervalTicks = 20;
    public boolean bestEffortLootStripOutsideLootTables = true;
    public boolean allowBookEnchanting = false;
    public String guaranteedSlotOrdering = "NEAREST_FIRST";
    public int maxBookshelfDiscountCount = 15;
    public double lapisDiscountPerBookshelf = 0.03;
    public double xpDiscountPerBookshelf = 0.03;

    public boolean enchantedBookLootEnabled = true;
    public double enchantedBookLootChance = 0.05;
    public double curseFailureChance = 0.15;
    public boolean consumeEnchantedBook = true;

    public static class EnchantmentCost {
        public int lapis;
        public int xp;
        public boolean isProtected = false;
        public int lootWeight = 1;

        public EnchantmentCost() {
        }

        public EnchantmentCost(int lapis, int xp) {
            this.lapis = lapis;
            this.xp = xp;
            this.isProtected = false;
            this.lootWeight = 1;
        }

        public EnchantmentCost(int lapis, int xp, boolean isProtected) {
            this.lapis = lapis;
            this.xp = xp;
            this.isProtected = isProtected;
            this.lootWeight = 1;
        }

        public EnchantmentCost(int lapis, int xp, boolean isProtected, int lootWeight) {
            this.lapis = lapis;
            this.xp = xp;
            this.isProtected = isProtected;
            this.lootWeight = lootWeight;
        }
    }

    public Map<String, EnchantmentCost> enchantmentCosts = new HashMap<>();

    public EnchantingSystemReImaginedConfig() {
        this.enchantmentCosts.put("default", new EnchantmentCost(10, 5));
        this.enchantmentCosts.put("minecraft:unbreaking", new EnchantmentCost(10, 5));
        this.enchantmentCosts.put("minecraft:efficiency", new EnchantmentCost(12, 7));
    }

    public EnchantmentCost getCost(String enchantmentId) {
        if (this.enchantmentCosts.containsKey(enchantmentId)) {
            return this.enchantmentCosts.get(enchantmentId);
        }
        if (this.enchantmentCosts.containsKey("default")) {
            return this.enchantmentCosts.get("default");
        }
        return new EnchantmentCost(10, 5);
    }

    public void updateFromRegistry() {
        boolean needsSave = false;
        if (this.enchantmentCosts == null) {
            this.enchantmentCosts = new HashMap<>();
        }
        if (!this.enchantmentCosts.containsKey("default")) {
            this.enchantmentCosts.put("default", new EnchantmentCost(10, 5));
            needsSave = true;
        }
        EnchantmentCost defaultCost = this.enchantmentCosts.get("default");

        try {
            for (Enchantment enchantment : Registries.ENCHANTMENT) {
                Identifier id = Registries.ENCHANTMENT.getId(enchantment);
                if (id != null) {
                    String key = id.toString();
                    if (!this.enchantmentCosts.containsKey(key)) {
                        int lootWeight = enchantment.isCursed() ? 0 : defaultCost.lootWeight;
                        this.enchantmentCosts.put(key, new EnchantmentCost(
                                defaultCost.lapis, defaultCost.xp, false, lootWeight));
                        needsSave = true;
                    } else {
                        EnchantmentCost existing = this.enchantmentCosts.get(key);
                        if (existing.lootWeight == 0 && !"default".equals(key) && !enchantment.isCursed()) {
                            existing.lootWeight = 1;
                            needsSave = true;
                        }
                        if (enchantment.isCursed() && existing.lootWeight > 0) {
                            existing.lootWeight = 0;
                            needsSave = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            EnchantingSystemReImagined.LOGGER.warn(
                    "[Enchanting System ReImagined] Registry not fully initialized yet, will re-try updates later.");
        }

        if (needsSave) {
            this.save();
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "enchanting-system-reimagined.json";
    private static EnchantingSystemReImaginedConfig instance;

    public static EnchantingSystemReImaginedConfig get() {
        if (instance == null) {
            instance = load();
            instance.updateFromRegistry();
        }
        return instance;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static EnchantingSystemReImaginedConfig load() {
        Path path = configPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                EnchantingSystemReImaginedConfig loaded = GSON.fromJson(reader, EnchantingSystemReImaginedConfig.class);
                if (loaded != null) {
                    boolean needsSave = false;
                    if (loaded.enchantmentCosts == null || loaded.enchantmentCosts.isEmpty()) {
                        loaded.enchantmentCosts = new HashMap<>();
                        loaded.enchantmentCosts.put("default", new EnchantmentCost(10, 5));
                        loaded.enchantmentCosts.put("minecraft:unbreaking", new EnchantmentCost(10, 5));
                        loaded.enchantmentCosts.put("minecraft:efficiency", new EnchantmentCost(12, 7));
                        needsSave = true;
                    }
                    if (needsSave) {
                        loaded.save();
                    }
                    return loaded;
                }
            } catch (IOException e) {
                EnchantingSystemReImagined.LOGGER
                        .warn("[Enchanting System ReImagined] Failed to read config, using defaults", e);
            }
        }
        EnchantingSystemReImaginedConfig fresh = new EnchantingSystemReImaginedConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            EnchantingSystemReImagined.LOGGER.warn("[Enchanting System ReImagined] Failed to write config", e);
        }
    }
}
