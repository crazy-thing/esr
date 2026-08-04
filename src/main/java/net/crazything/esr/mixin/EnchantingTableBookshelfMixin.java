package net.crazything.esr.mixin;

import net.crazything.esr.config.EnchantingSystemReImaginedConfig;
import net.crazything.esr.util.ChiseledBookshelfScanner;
import net.crazything.esr.util.EnchantmentScreenHandlerAccess;
import net.crazything.esr.util.EnchantmentCapUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantingTableBookshelfMixin extends ScreenHandler implements EnchantmentScreenHandlerAccess {

    protected EnchantingTableBookshelfMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    @Final
    private Inventory inventory;
    @Shadow
    @Final
    private ScreenHandlerContext context;
    @Shadow
    public int[] enchantmentId;
    @Shadow
    public int[] enchantmentLevel;
    @Shadow
    public int[] enchantmentPower;
    @Shadow
    @Final
    private Property seed;

    @Shadow
    public abstract void onContentChanged(Inventory inventory);

    @Shadow
    protected abstract List<EnchantmentLevelEntry> generateEnchantments(ItemStack stack, int slot, int level);

    @Unique
    private final Property esr$scrollOffset = Property.create();
    @Unique
    private final Property esr$totalEnchantments = Property.create();

    @Unique
    private final Property esr$xpCost0 = Property.create();
    @Unique
    private final Property esr$xpCost1 = Property.create();
    @Unique
    private final Property esr$xpCost2 = Property.create();

    @Unique
    private final List<Enchantment> esr$allGuaranteed = new ArrayList<>();
    @Unique
    private ItemStack esr$lastTarget = ItemStack.EMPTY;

    @Unique
    private final int[] esr$originalPower = new int[3];

    @Override
    public int esr$getScrollOffset() {
        return this.esr$scrollOffset.get();
    }

    @Override
    public int esr$getTotalEnchantments() {
        return this.esr$totalEnchantments.get();
    }

    @Override
    public int esr$getXpCost(int slot) {
        if (slot == 0)
            return this.esr$xpCost0.get();
        if (slot == 1)
            return this.esr$xpCost1.get();
        if (slot == 2)
            return this.esr$xpCost2.get();
        return 5;
    }

    @Unique
    private void esr$setXpCost(int slot, int value) {
        if (slot == 0)
            this.esr$xpCost0.set(value);
        else if (slot == 1)
            this.esr$xpCost1.set(value);
        else if (slot == 2)
            this.esr$xpCost2.set(value);
    }

    @Unique
    private boolean esr$isLoading = false;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("RETURN"))
    private void esr$initProperties(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context,
            CallbackInfo ci) {
        this.addProperty(this.esr$scrollOffset);
        this.addProperty(this.esr$totalEnchantments);
        this.addProperty(this.esr$xpCost0);
        this.addProperty(this.esr$xpCost1);
        this.addProperty(this.esr$xpCost2);
        context.run((world, pos) -> {
            net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof Inventory) {
                Inventory blockEntityInv = (Inventory) be;
                this.esr$isLoading = true;
                this.inventory.setStack(0, blockEntityInv.getStack(0).copy());
                this.inventory.setStack(1, blockEntityInv.getStack(1).copy());
                this.esr$isLoading = false;
            }
        });
    }

    @Unique
    private void esr$syncToClients() {
        this.sendContentUpdates();
    }

    @Unique
    private final Enchantment[] esr$guaranteedBySlot = new Enchantment[3];

    @Inject(method = "onContentChanged", at = @At("HEAD"))
    private void esr$saveInventoryToBlockEntity(Inventory changedInventory, CallbackInfo ci) {
        if (this.esr$isLoading) {
            return;
        }
        if (changedInventory == this.inventory) {
            this.context.run((world, pos) -> {
                net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof Inventory) {
                    Inventory blockEntityInv = (Inventory) be;
                    blockEntityInv.setStack(0, this.inventory.getStack(0).copy());
                    blockEntityInv.setStack(1, this.inventory.getStack(1).copy());
                    blockEntityInv.markDirty();
                }
            });
        }
    }

    @Inject(method = "onContentChanged", at = @At("TAIL"))
    private void esr$injectGuaranteedEnchantments(Inventory changedInventory, CallbackInfo ci) {
        Arrays.fill(this.esr$guaranteedBySlot, null);

        if (changedInventory != this.inventory) {
            return;
        }
        ItemStack target = this.inventory.getStack(0);
        if (target.isEmpty()) {
            this.esr$allGuaranteed.clear();
            this.esr$scrollOffset.set(0);
            this.esr$totalEnchantments.set(0);
            this.esr$setXpCost(0, 0);
            this.esr$setXpCost(1, 0);
            this.esr$setXpCost(2, 0);
            this.esr$lastTarget = ItemStack.EMPTY;
            return;
        }

        if (!ItemStack.areItemsEqual(target, this.esr$lastTarget)) {
            this.esr$scrollOffset.set(0);
            this.esr$lastTarget = target.copy();
        }

        if (!EnchantingSystemReImaginedConfig.get().allowBookEnchanting && target.isOf(net.minecraft.item.Items.BOOK)) {
            for (int i = 0; i < 3; i++) {
                this.enchantmentId[i] = -1;
                this.enchantmentLevel[i] = -1;
                this.enchantmentPower[i] = 0;
                this.esr$originalPower[i] = 0;
                this.esr$setXpCost(i, 0);
            }
            this.esr$allGuaranteed.clear();
            this.esr$totalEnchantments.set(0);
            this.esr$scrollOffset.set(0);
            this.esr$syncToClients();
            return;
        }

        this.context.run((world, pos) -> {
            if (!EnchantmentHelper.get(target).isEmpty()) {
                for (int i = 0; i < 3; i++) {
                    this.enchantmentId[i] = -1;
                    this.enchantmentLevel[i] = -1;
                    this.enchantmentPower[i] = 0;
                    this.esr$originalPower[i] = 0;
                    this.esr$setXpCost(i, 0);
                }
                this.esr$allGuaranteed.clear();
                this.esr$totalEnchantments.set(0);
                this.esr$scrollOffset.set(0);
                this.esr$syncToClients();
                return;
            }

            List<ChiseledBookshelfScanner.GuaranteedSource> sources = ChiseledBookshelfScanner.scan(world, pos);

            if (!sources.isEmpty()) {
                LinkedHashMap<Enchantment, Integer> guaranteedFlat = new LinkedHashMap<>();
                for (ChiseledBookshelfScanner.GuaranteedSource source : sources) {
                    for (Map.Entry<Enchantment, Integer> entry : source.enchantments().entrySet()) {
                        guaranteedFlat.putIfAbsent(entry.getKey(), entry.getValue());
                    }
                }

                this.esr$allGuaranteed.clear();
                for (Map.Entry<Enchantment, Integer> entry : guaranteedFlat.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    if (enchantment.isAcceptableItem(target)) {
                        this.esr$allGuaranteed.add(enchantment);
                    }
                }

                int total = this.esr$allGuaranteed.size();
                this.esr$totalEnchantments.set(total);

                if (total > 0) {
                    int offset = this.esr$scrollOffset.get();
                    if (offset < 0) {
                        offset = 0;
                    } else if (offset > Math.max(0, total - 3)) {
                        offset = Math.max(0, total - 3);
                    }
                    this.esr$scrollOffset.set(offset);

                    int bookshelves = ChiseledBookshelfScanner.countRegularBookshelves(world, pos);
                    EnchantingSystemReImaginedConfig config = EnchantingSystemReImaginedConfig.get();
                    int clampedShelves = Math.min(bookshelves, config.maxBookshelfDiscountCount);
                    double lapisDiscountFactor = Math.max(0.0,
                            1.0 - (clampedShelves * config.lapisDiscountPerBookshelf));
                    double xpDiscountFactor = Math.max(0.0, 1.0 - (clampedShelves * config.xpDiscountPerBookshelf));

                    for (int slot = 0; slot < 3; slot++) {
                        int index = offset + slot;
                        if (index < total) {
                            Enchantment enchantment = this.esr$allGuaranteed.get(index);
                            int rawId = Registries.ENCHANTMENT.getRawId(enchantment);
                            Identifier encId = Registries.ENCHANTMENT.getId(enchantment);

                            this.enchantmentId[slot] = rawId;
                            this.enchantmentLevel[slot] = enchantment.getMaxLevel();

                            int lapisCost = 10;
                            int xpCost = 5;
                            if (encId != null) {
                                EnchantingSystemReImaginedConfig.EnchantmentCost cost = config
                                        .getCost(encId.toString());
                                lapisCost = cost.lapis;
                                xpCost = cost.xp;
                            }

                            int finalLapisCost = Math.max(1, (int) Math.round(lapisCost * lapisDiscountFactor));
                            int finalXpCost = Math.max(1, (int) Math.round(xpCost * xpDiscountFactor));

                            this.enchantmentPower[slot] = finalLapisCost;
                            this.esr$setXpCost(slot, finalXpCost);
                            this.esr$guaranteedBySlot[slot] = enchantment;
                        } else {
                            this.enchantmentId[slot] = -1;
                            this.enchantmentLevel[slot] = -1;
                            this.enchantmentPower[slot] = 0;
                            this.esr$setXpCost(slot, 0);
                        }
                    }
                    this.esr$syncToClients();
                    return;
                }
            }
            this.esr$allGuaranteed.clear();
            this.esr$totalEnchantments.set(0);
            this.esr$scrollOffset.set(0);
            for (int slot = 0; slot < 3; slot++) {
                this.enchantmentId[slot] = -1;
                this.enchantmentLevel[slot] = -1;
                this.enchantmentPower[slot] = 0;
                this.esr$originalPower[slot] = 0;
                this.esr$setXpCost(slot, 0);
            }
            this.esr$syncToClients();
        });
    }

    @Inject(method = "onButtonClick", at = @At("HEAD"), cancellable = true)
    private void esr$handleButtonClickHead(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (id == 3) {
            int current = this.esr$scrollOffset.get();
            if (current > 0) {
                this.esr$scrollOffset.set(current - 1);
                this.onContentChanged(this.inventory);
            }
            cir.setReturnValue(true);
            return;
        }
        if (id == 4) {
            int current = this.esr$scrollOffset.get();
            int total = this.esr$totalEnchantments.get();
            if (current + 3 < total) {
                this.esr$scrollOffset.set(current + 1);
                this.onContentChanged(this.inventory);
            }
            cir.setReturnValue(true);
            return;
        }

        if (id >= 0 && id < 3) {
            ItemStack target = this.inventory.getStack(0);
            if (!EnchantingSystemReImaginedConfig.get().allowBookEnchanting
                    && target.isOf(net.minecraft.item.Items.BOOK)) {
                cir.setReturnValue(false);
                return;
            }
            ItemStack lapis = this.inventory.getStack(1);

            Enchantment guaranteed = this.esr$guaranteedBySlot[id];
            int rawId = this.enchantmentId[id];
            Enchantment enchantment = (guaranteed != null) ? guaranteed : Enchantment.byRawId(rawId);

            if (enchantment != null) {
                int finalLapisCost = this.enchantmentPower[id];
                int finalXpCost = this.esr$getXpCost(id);

                if (!player.getAbilities().creativeMode) {
                    if (player.experienceLevel < finalXpCost || lapis.getCount() < finalLapisCost) {
                        cir.setReturnValue(false);
                        return;
                    }
                }

                if (!player.getAbilities().creativeMode) {
                    player.applyEnchantmentCosts(target, finalXpCost);
                    lapis.decrement(finalLapisCost);
                    if (lapis.isEmpty()) {
                        this.inventory.setStack(1, ItemStack.EMPTY);
                    }
                }

                ItemStack result = target;
                boolean isBook = target.isOf(net.minecraft.item.Items.BOOK);
                if (isBook) {
                    result = new ItemStack(net.minecraft.item.Items.ENCHANTED_BOOK);
                    if (target.hasNbt()) {
                        result.setNbt(target.getNbt().copy());
                    }
                    this.inventory.setStack(0, result);
                }

                if (guaranteed != null) {
                    Map<Enchantment, Integer> single = new LinkedHashMap<>();
                    single.put(guaranteed, guaranteed.getMaxLevel());
                    EnchantmentHelper.set(single, result);

                    double failChance = EnchantingSystemReImaginedConfig.get().curseFailureChance;
                    if (failChance > 0.0 && player.getWorld().getRandom().nextDouble() < failChance) {
                        List<Enchantment> curses = new java.util.ArrayList<>();
                        for (Enchantment enc : Registries.ENCHANTMENT) {
                            if (enc.isCursed())
                                curses.add(enc);
                        }
                        if (!curses.isEmpty()) {
                            Enchantment curse = curses.get(player.getWorld().getRandom().nextInt(curses.size()));
                            if (isBook) {
                                net.minecraft.item.EnchantedBookItem.addEnchantment(result,
                                        new EnchantmentLevelEntry(curse, curse.getMaxLevel()));
                            } else {
                                result.addEnchantment(curse, curse.getMaxLevel());
                            }
                        }
                    }
                } else {
                    int power = this.esr$originalPower[id];
                    List<EnchantmentLevelEntry> list = this.generateEnchantments(target, id, power);
                    if (!list.isEmpty()) {
                        if (isBook) {
                            for (EnchantmentLevelEntry entry : list) {
                                net.minecraft.item.EnchantedBookItem.addEnchantment(result, entry);
                            }
                        } else {
                            for (EnchantmentLevelEntry entry : list) {
                                result.addEnchantment(entry.enchantment, entry.level);
                            }
                        }
                        EnchantmentCapUtil.enforceSingleEnchantment(result);
                    }
                }

                result.getOrCreateNbt().putBoolean("esr$PlayerOwned", true);

                player.incrementStat(net.minecraft.stat.Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayerEntity) {
                    net.minecraft.advancement.criterion.Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player,
                            result, finalXpCost);
                }
                this.inventory.markDirty();
                this.seed.set(player.getEnchantmentTableSeed());
                this.onContentChanged(this.inventory);

                this.context.run((world, pos) -> {
                    if (guaranteed != null && EnchantingSystemReImaginedConfig.get().consumeEnchantedBook) {
                        net.crazything.esr.util.ChiseledBookshelfScanner.consumeEnchantment(world, pos, guaranteed);
                    }
                    world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                            net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
                });

                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "onClosed", at = @At("HEAD"), cancellable = true)
    private void esr$onClosed(PlayerEntity player, CallbackInfo ci) {
        super.onClosed(player);
        this.context.run((world, pos) -> {
            net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof Inventory) {
                Inventory blockEntityInv = (Inventory) be;
                blockEntityInv.setStack(0, this.inventory.getStack(0).copy());
                blockEntityInv.setStack(1, this.inventory.getStack(1).copy());
                blockEntityInv.markDirty();
            }
        });
        ci.cancel();
    }
}
