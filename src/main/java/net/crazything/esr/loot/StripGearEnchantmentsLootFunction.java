package net.crazything.esr.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.crazything.esr.util.EnchantmentCapUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StripGearEnchantmentsLootFunction implements LootFunction {

    public static final StripGearEnchantmentsLootFunction INSTANCE = new StripGearEnchantmentsLootFunction();

    public static final LootFunctionType TYPE = new LootFunctionType(
            new JsonSerializer<StripGearEnchantmentsLootFunction>() {
                @Override
                public void toJson(JsonObject json, StripGearEnchantmentsLootFunction object,
                        JsonSerializationContext context) {
                }

                @Override
                public StripGearEnchantmentsLootFunction fromJson(JsonObject json, JsonDeserializationContext context) {
                    return INSTANCE;
                }
            });

    private StripGearEnchantmentsLootFunction() {
    }

    public static void register(Identifier id) {
        net.minecraft.registry.Registry.register(
                net.minecraft.registry.Registries.LOOT_FUNCTION_TYPE, id, TYPE);
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        if (GearItemDetection.isGearItem(stack)) {
            stack.removeSubNbt("Enchantments");
            stack.removeSubNbt("StoredEnchantments");
        } else if (GearItemDetection.isEnchantedBook(stack)) {
            if (stack.isOf(net.minecraft.item.Items.BOOK)) {
                ItemStack enchantedBook = new ItemStack(net.minecraft.item.Items.ENCHANTED_BOOK, stack.getCount());
                if (stack.hasNbt()) {
                    enchantedBook.setNbt(stack.getNbt().copy());
                }
                stack = enchantedBook;
            }
            stack.removeSubNbt("Enchantments");
            removeCurses(stack);

            Map<Enchantment, Integer> current = EnchantmentCapUtil.getEnchantments(stack);
            if (current.isEmpty()) {
                EnchantmentCapUtil.enforceSingleEnchantment(stack);
            } else if (!EnchantmentCapUtil.isCompliant(stack)) {
                EnchantmentCapUtil.enforceSingleEnchantment(stack);
            }
        } else {
            stack.removeSubNbt("StoredEnchantments");
            removeCurses(stack);
        }
        return stack;
    }

    private void removeCurses(ItemStack stack) {
        if (stack.isEmpty())
            return;
        Map<Enchantment, Integer> enchants = EnchantmentCapUtil.getEnchantments(stack);
        boolean modified = false;
        Map<Enchantment, Integer> newEnchants = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            if (entry.getKey().isCursed()) {
                modified = true;
            } else {
                newEnchants.put(entry.getKey(), entry.getValue());
            }
        }
        if (modified) {
            EnchantmentCapUtil.setEnchantments(newEnchants, stack);
        }
    }

    @Override
    public LootFunctionType getType() {
        return TYPE;
    }
}
