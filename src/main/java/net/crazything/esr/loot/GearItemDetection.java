package net.crazything.esr.loot;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.TridentItem;

public final class GearItemDetection {

    private GearItemDetection() {
    }

    public static boolean isGearItem(Item item) {
        if (item instanceof EnchantedBookItem) {
            return false;
        }
        return item instanceof ArmorItem
                || item instanceof ToolItem
                || item instanceof FishingRodItem
                || item instanceof RangedWeaponItem
                || item instanceof TridentItem;
    }

    public static boolean isEnchantedBook(Item item) {
        return item instanceof EnchantedBookItem;
    }
}
