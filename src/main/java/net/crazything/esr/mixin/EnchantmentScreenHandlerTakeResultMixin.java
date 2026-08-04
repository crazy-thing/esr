package net.crazything.esr.mixin;

import net.crazything.esr.util.EnchantmentCapUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerTakeResultMixin {

    @Inject(method = "onButtonClick", at = @At("RETURN"), cancellable = false)
    private void esr$capTableResult(net.minecraft.entity.player.PlayerEntity player,
            int id, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }

        EnchantmentScreenHandler self = (EnchantmentScreenHandler) (Object) this;
        Slot resultSlot = self.getSlot(0);
        if (resultSlot == null) {
            return;
        }
        ItemStack result = resultSlot.getStack();
        if (!result.isEmpty()) {
            EnchantmentCapUtil.enforceSingleEnchantment(result);
        }
    }
}
