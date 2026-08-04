package net.crazything.esr.mixin;

import net.crazything.esr.util.EnchantmentCapUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TradeOffers.EnchantBookFactory.class)
public class SellEnchantedBookFactoryMixin {

    @Inject(method = "create", at = @At("RETURN"))
    private void esr$capTradeBook(Entity entity, Random random,
            CallbackInfoReturnable<TradeOffer> cir) {
        TradeOffer offer = cir.getReturnValue();
        if (offer == null) {
            return;
        }
        EnchantmentCapUtil.enforceSingleEnchantment(offer.getSellItem());
    }
}
