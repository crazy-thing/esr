package net.crazything.esr.util;

import net.crazything.esr.config.EnchantingSystemReImaginedConfig;
import net.crazything.esr.loot.GearItemDetection;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class EnchantmentCapUtil {

    private EnchantmentCapUtil() {
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        if (stack.isEmpty()) {
            return new LinkedHashMap<>();
        }
        if (stack.hasNbt() && stack.getNbt().contains("StoredEnchantments")) {
            return EnchantmentHelper.fromNbt(EnchantedBookItem.getEnchantmentNbt(stack));
        }
        return EnchantmentHelper.get(stack);
    }

    public static void setEnchantments(Map<Enchantment, Integer> enchantments, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (GearItemDetection.isEnchantedBook(stack)) {
            stack.removeSubNbt("Enchantments");
            NbtList nbtList = new NbtList();
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                if (entry.getKey() != null) {
                    Identifier id = Registries.ENCHANTMENT.getId(entry.getKey());
                    if (id != null) {
                        nbtList.add(EnchantmentHelper.createNbt(id, entry.getValue()));
                    }
                }
            }
            if (nbtList.isEmpty()) {
                stack.removeSubNbt("StoredEnchantments");
            } else {
                stack.setSubNbt("StoredEnchantments", nbtList);
            }
        } else {
            stack.removeSubNbt("StoredEnchantments");
            EnchantmentHelper.set(enchantments, stack);
        }
    }

    public static void enforceSingleEnchantment(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        boolean isBook = GearItemDetection.isEnchantedBook(stack);
        Map<Enchantment, Integer> current = getEnchantments(stack);

        if (isBook) {
            Map<Enchantment, Integer> nonCursed = new LinkedHashMap<>();
            for (Map.Entry<Enchantment, Integer> entry : current.entrySet()) {
                if (!entry.getKey().isCursed()) {
                    nonCursed.put(entry.getKey(), entry.getValue());
                }
            }

            if (nonCursed.isEmpty()) {
                List<Enchantment> valid = new ArrayList<>();
                EnchantingSystemReImaginedConfig cfg = EnchantingSystemReImaginedConfig.get();
                for (Enchantment enchantment : Registries.ENCHANTMENT) {
                    if (!enchantment.isCursed()) {
                        Identifier id = Registries.ENCHANTMENT.getId(enchantment);
                        if (id != null) {
                            EnchantingSystemReImaginedConfig.EnchantmentCost cost = cfg.getCost(id.toString());
                            if (cost != null && cost.lootWeight <= 0) {
                                continue;
                            }
                        }
                        valid.add(enchantment);
                    }
                }
                if (!valid.isEmpty()) {
                    Random rand = new Random();
                    Enchantment selected = valid.get(rand.nextInt(valid.size()));
                    Map<Enchantment, Integer> map = new LinkedHashMap<>();
                    map.put(selected, selected.getMaxLevel());
                    setEnchantments(map, stack);
                } else {
                    setEnchantments(new LinkedHashMap<>(), stack);
                }
                return;
            }
        }

        if (current.isEmpty()) {
            return;
        }

        Map<Enchantment, Integer> result = new LinkedHashMap<>();
        boolean foundNonCursed = false;

        for (Map.Entry<Enchantment, Integer> entry : current.entrySet()) {
            Enchantment ench = entry.getKey();
            if (ench.isCursed()) {
                result.put(ench, ench.getMaxLevel());
            } else {
                if (!foundNonCursed) {
                    result.put(ench, ench.getMaxLevel());
                    foundNonCursed = true;
                }
            }
        }

        setEnchantments(result, stack);
    }

    public static boolean isCompliant(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        boolean isBook = GearItemDetection.isEnchantedBook(stack);
        Map<Enchantment, Integer> current = getEnchantments(stack);

        if (isBook && current.isEmpty()) {
            return false;
        }
        if (current.isEmpty()) {
            return true;
        }

        int nonCursedCount = 0;
        for (Map.Entry<Enchantment, Integer> entry : current.entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();
            if (ench.isCursed()) {
                if (level != ench.getMaxLevel()) {
                    return false;
                }
            } else {
                nonCursedCount++;
                if (nonCursedCount > 1) {
                    return false;
                }
                if (level != ench.getMaxLevel()) {
                    return false;
                }
            }
        }
        return true;
    }
}

