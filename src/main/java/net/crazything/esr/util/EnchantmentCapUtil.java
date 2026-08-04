package net.crazything.esr.util;

import net.crazything.esr.loot.GearItemDetection;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class EnchantmentCapUtil {

    private EnchantmentCapUtil() {
    }

    public static void enforceSingleEnchantment(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (GearItemDetection.isEnchantedBook(stack.getItem())) {
            Map<Enchantment, Integer> current = EnchantmentHelper.get(stack);
            if (current.isEmpty()) {
                List<Enchantment> valid = new ArrayList<>();
                for (Enchantment enchantment : Registries.ENCHANTMENT) {
                    if (!enchantment.isCursed()) {
                        valid.add(enchantment);
                    }
                }
                if (!valid.isEmpty()) {
                    Random rand = new Random();
                    Enchantment selected = valid.get(rand.nextInt(valid.size()));
                    Map<Enchantment, Integer> map = new LinkedHashMap<>();
                    map.put(selected, selected.getMaxLevel());
                    EnchantmentHelper.set(map, stack);
                }
                return;
            }
        }

        Map<Enchantment, Integer> current = EnchantmentHelper.get(stack);
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

        EnchantmentHelper.set(result, stack);
    }

    public static boolean isCompliant(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (GearItemDetection.isEnchantedBook(stack.getItem())) {
            Map<Enchantment, Integer> current = EnchantmentHelper.get(stack);
            if (current.isEmpty()) {
                return false;
            }
        }
        Map<Enchantment, Integer> current = EnchantmentHelper.get(stack);
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
