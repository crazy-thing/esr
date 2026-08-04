package net.crazything.esr.mixin;

import net.crazything.esr.util.EnchantmentScreenHandlerAccess;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EnchantmentScreen.class, priority = 9999)
public abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler> {

    public EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Unique
    private int esr$mouseX;
    @Unique
    private int esr$mouseY;

    @Inject(method = "drawBackground", at = @At("HEAD"))
    private void esr$captureMouse(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        this.esr$mouseX = mouseX;
        this.esr$mouseY = mouseY;
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void esr$drawScrollbar(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        int trackX = x + 172;
        int trackY = y + 14;
        int trackWidth = 4;
        int trackHeight = 57;

        EnchantmentScreenHandler handler = this.handler;
        if (!(handler instanceof EnchantmentScreenHandlerAccess access)) {
            return;
        }

        int total = access.esr$getTotalEnchantments();
        if (total <= 3) {
            return;
        }

        int offset = access.esr$getScrollOffset();
        context.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF4A4A4A);

        int visibleCount = 3;
        int thumbHeight = Math.max(10, (visibleCount * trackHeight) / total);
        int maxScroll = total - 3;
        int scrollProgressHeight = trackHeight - thumbHeight;
        int thumbY = trackY + (offset * scrollProgressHeight) / maxScroll;

        context.fill(trackX, thumbY, trackX + trackWidth, thumbY + thumbHeight, 0xFFC0C0C0);
    }

    @Redirect(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
    private void esr$redirectDrawTexture(DrawContext context, Identifier texture, int x, int y, int u, int v, int width,
            int height) {
        if (texture.getPath().contains("enchanting_table") && (v == 223 || v == 239) && width == 16 && height == 16) {
            int guiTop = (this.height - this.backgroundHeight) / 2;
            int slot = (y - 15 - guiTop) / 19;
            int lapisCost = this.handler.enchantmentPower[slot];

            int xpCost = 5;
            if (this.handler instanceof EnchantmentScreenHandlerAccess access) {
                xpCost = access.esr$getXpCost(slot);
            }

            boolean hasEnoughXP = this.client.player.getAbilities().creativeMode
                    || this.client.player.experienceLevel >= xpCost;
            boolean hasEnoughLapis = this.client.player.getAbilities().creativeMode
                    || this.handler.getLapisCount() >= lapisCost;
            int guiLeft = (this.width - this.backgroundWidth) / 2;
            int targetX = guiLeft + 64;
            int targetY = guiTop + 14 + 19 * slot + 6;

            int color;
            if (hasEnoughXP && hasEnoughLapis) {
                color = 8453920 | 0xFF000000;
            } else {
                color = 0xFF685F4A;
            }
            context.getMatrices().push();
            context.getMatrices().scale(0.9F, 0.9F, 1.0F);
            context.drawTextWithShadow(this.textRenderer, String.valueOf(xpCost), (int) (targetX / 0.9F),
                    (int) (targetY / 0.9F), color);
            context.getMatrices().pop();
        } else {
            context.drawTexture(texture, x, y, u, v, width, height);
        }
    }

    @Unique
    private String esr$truncateName(String name, int maxWidth) {
        if (this.textRenderer.getWidth(name) <= maxWidth) {
            return name;
        }
        String truncated = name;
        while (truncated.length() > 0 && this.textRenderer.getWidth(truncated + "...") > maxWidth) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        return truncated + "...";
    }

    @Redirect(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWrapped(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/StringVisitable;IIII)V"))
    private void esr$redirectDrawTextWrapped(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer,
            net.minecraft.text.StringVisitable text, int x, int y, int width, int color) {
        int guiTop = (this.height - this.backgroundHeight) / 2;
        int slot = (y - 16 - guiTop) / 19;

        if (slot >= 0 && slot < 3) {
            int rawId = this.handler.enchantmentId[slot];
            int level = this.handler.enchantmentLevel[slot];
            Enchantment enchantment = Enchantment.byRawId(rawId);

            if (enchantment != null) {
                String nameStr = enchantment.getName(level).getString();
                int maxAreaWidth = 82;
                String finalName = this.esr$truncateName(nameStr, maxAreaWidth);
                int guiLeft = (this.width - this.backgroundWidth) / 2;
                int drawX = (guiLeft + 73) + (maxAreaWidth - this.textRenderer.getWidth(finalName)) / 2;
                int targetY = guiTop + 14 + 19 * slot + 5;
                int lapisCost = this.handler.enchantmentPower[slot];
                int xpCost = 5;
                if (this.handler instanceof EnchantmentScreenHandlerAccess access) {
                    xpCost = access.esr$getXpCost(slot);
                }

                boolean hasEnoughXP = this.client.player.getAbilities().creativeMode
                        || this.client.player.experienceLevel >= xpCost;
                boolean hasEnoughLapis = this.client.player.getAbilities().creativeMode
                        || this.handler.getLapisCount() >= lapisCost;

                int finalColor;
                if (!hasEnoughXP || !hasEnoughLapis) {
                    finalColor = 0xFF685F4A;
                } else {
                    int mouseXRel = this.esr$mouseX - guiLeft;
                    int mouseYRel = this.esr$mouseY - guiTop;
                    int slotYStart = 14 + 19 * slot;
                    if (mouseXRel >= 60 && mouseXRel <= 168 && mouseYRel >= slotYStart && mouseYRel < slotYStart + 19) {
                        finalColor = 16777088 | 0xFF000000;
                    } else {
                        finalColor = 0xFFD0D0D0;
                    }
                }
                context.drawText(textRenderer, finalName, drawX, targetY, finalColor, false);
                return;
            }
        }
        context.drawTextWrapped(textRenderer, text, x, y, width, color);
    }

    @Redirect(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"))
    private int esr$redirectDrawText(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer,
            String text, int x, int y, int color) {
        int guiTop = (this.height - this.backgroundHeight) / 2;
        int slot = (y - 7 - 16 - guiTop) / 19;
        if (slot >= 0 && slot < 3) {
            int lapisCost = this.handler.enchantmentPower[slot];

            int xpCost = 5;
            if (this.handler instanceof EnchantmentScreenHandlerAccess access) {
                xpCost = access.esr$getXpCost(slot);
            }

            boolean hasEnoughXP = this.client.player.getAbilities().creativeMode
                    || this.client.player.experienceLevel >= xpCost;
            boolean hasEnoughLapis = this.client.player.getAbilities().creativeMode
                    || this.handler.getLapisCount() >= lapisCost;

            if (!hasEnoughXP || !hasEnoughLapis) {
                color = 0x685F4A;
            } else {
                color = 0x506DFF;
            }

            int guiLeft = (this.width - this.backgroundWidth) / 2;
            int textWidth = textRenderer.getWidth(text);
            int targetX = guiLeft + 164 - (int) (textWidth * 0.9F);
            int targetY = guiTop + 14 + 19 * slot + 6;
            context.getMatrices().push();
            context.getMatrices().scale(0.9F, 0.9F, 1.0F);
            context.drawTextWithShadow(textRenderer, text, (int) (targetX / 0.9F), (int) (targetY / 0.9F), color);
            context.getMatrices().pop();
            return 0;
        }
        return context.drawTextWithShadow(textRenderer, text, x, y, color);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;II)V"))
    private void esr$redirectDrawTooltip(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer,
            List<Text> text, int mouseX, int mouseY) {
        int guiLeft = (this.width - this.backgroundWidth) / 2;
        int guiTop = (this.height - this.backgroundHeight) / 2;

        for (int i = 0; i < 3; i++) {
            if (this.isPointWithinBounds(60, 14 + 19 * i, 108, 19, mouseX, mouseY)
                    && this.handler.enchantmentPower[i] > 0) {
                int rawId = this.handler.enchantmentId[i];
                Enchantment enchantment = Enchantment.byRawId(rawId);
                if (enchantment != null) {
                    int level = this.handler.enchantmentLevel[i];
                    List<Text> cleanList = new ArrayList<>();
                    cleanList.add(Text.translatable("container.enchant.clue", enchantment.getName(level)));

                    context.drawTooltip(textRenderer, cleanList, mouseX, mouseY);
                    return;
                }
            }
        }

        context.drawTooltip(textRenderer, text, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void esr$handleScrollbarClick(double mouseX, double mouseY, int button,
            CallbackInfoReturnable<Boolean> cir) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        double relativeX = mouseX - x;
        double relativeY = mouseY - y;

        if (relativeX >= 170 && relativeX <= 178 && relativeY >= 14 && relativeY <= 71) {
            EnchantmentScreenHandler handler = this.handler;
            if (handler instanceof EnchantmentScreenHandlerAccess access) {
                int total = access.esr$getTotalEnchantments();
                if (total > 3) {
                    int offset = access.esr$getScrollOffset();
                    int trackHeight = 57;
                    int trackY = y + 14;
                    int thumbHeight = Math.max(10, (3 * trackHeight) / total);
                    int maxScroll = total - 3;
                    int scrollProgressHeight = trackHeight - thumbHeight;
                    int thumbY = trackY + (offset * scrollProgressHeight) / maxScroll;
                    int thumbCenterY = thumbY + thumbHeight / 2;

                    if (mouseY < thumbCenterY) {
                        if (offset > 0) {
                            this.client.interactionManager.clickButton(this.handler.syncId, 3);
                        }
                    } else {
                        if (offset + 3 < total) {
                            this.client.interactionManager.clickButton(this.handler.syncId, 4);
                        }
                    }
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        double relativeX = mouseX - x;
        double relativeY = mouseY - y;

        if (relativeX >= 60 && relativeX <= 180 && relativeY >= 14 && relativeY <= 71) {
            EnchantmentScreenHandler handler = this.handler;
            if (handler instanceof EnchantmentScreenHandlerAccess access) {
                int total = access.esr$getTotalEnchantments();
                if (total > 3) {
                    int offset = access.esr$getScrollOffset();
                    if (amount > 0) {
                        if (offset > 0) {
                            this.client.interactionManager.clickButton(this.handler.syncId, 3);
                        }
                    } else if (amount < 0) {
                        if (offset + 3 < total) {
                            this.client.interactionManager.clickButton(this.handler.syncId, 4);
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
