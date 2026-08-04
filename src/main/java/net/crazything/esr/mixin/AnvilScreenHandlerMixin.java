package net.crazything.esr.mixin;

import net.crazything.esr.util.EnchantmentCapUtil;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {

    @Shadow
    @Final
    private Property levelCost;

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void esr$capAnvilOutput(CallbackInfo ci) {
        AnvilScreenHandler self = (AnvilScreenHandler) (Object) this;

        ItemStack input1 = self.getSlot(0).getStack();
        ItemStack input2 = self.getSlot(1).getStack();

        boolean isInput1Book = input1.getItem() instanceof EnchantedBookItem
                || input1.getSubNbt("StoredEnchantments") != null;
        boolean isInput2Book = input2.getItem() instanceof EnchantedBookItem
                || input2.getSubNbt("StoredEnchantments") != null;

        if (isInput1Book || isInput2Book) {
            self.getSlot(2).setStack(ItemStack.EMPTY);
            this.levelCost.set(0);
            ((ScreenHandler) (Object) self).sendContentUpdates();
            return;
        }

        Slot outputSlot = self.getSlot(2);
        if (outputSlot == null) {
            return;
        }
        ItemStack output = outputSlot.getStack();
        if (!output.isEmpty()) {
            output.getOrCreateNbt().putBoolean("esr$PlayerOwned", true);
            EnchantmentCapUtil.enforceSingleEnchantment(output);
        }
    }
}
