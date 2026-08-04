package net.crazything.esr.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentNameMixin {

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void esr$suppressNumeral(int level, CallbackInfoReturnable<Text> cir) {
        Enchantment self = (Enchantment) (Object) this;
        MutableText text = Text.translatable(self.getTranslationKey());
        text.formatted(self.isCursed() ? Formatting.RED : Formatting.GRAY);
        cir.setReturnValue(text);
    }
}
