package net.crazything.esr.loot;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.TridentItem;

public final class GearItemDetection {

    private GearItemDetection() {
    }

    public static boolean isGearItem(Item item) {
        if (item instanceof EnchantedBookItem || item == Items.ENCHANTED_BOOK) {
            return false;
        }
        return item instanceof ArmorItem
                || item instanceof ToolItem
                || item instanceof FishingRodItem
                || item instanceof RangedWeaponItem
                || item instanceof TridentItem
                || item instanceof ShearsItem
                || item instanceof ShieldItem
                || item instanceof ElytraItem;
    }

    public static boolean isGearItem(ItemStack stack) {
        if (stack.isEmpty() || isEnchantedBook(stack)) {
            return false;
        }
        if (isGearItem(stack.getItem())) {
            return true;
        }
        return stack.hasEnchantments() || (stack.hasNbt() && stack.getNbt().contains("Enchantments"));
    }

    public static boolean isEnchantedBook(Item item) {
        return item instanceof EnchantedBookItem || item == Items.ENCHANTED_BOOK;
    }

    public static boolean isEnchantedBook(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (isEnchantedBook(stack.getItem())) {
            return true;
        }
        return stack.hasNbt() && stack.getNbt().contains("StoredEnchantments");
    }
}

