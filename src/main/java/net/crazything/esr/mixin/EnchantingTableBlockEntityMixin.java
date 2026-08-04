package net.crazything.esr.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantingTableBlockEntity.class)
public abstract class EnchantingTableBlockEntityMixin extends BlockEntity implements Inventory {

    public EnchantingTableBlockEntityMixin(BlockPos pos, BlockState state) {
        super(BlockEntityType.ENCHANTING_TABLE, pos, state);
    }

    @Unique
    private final DefaultedList<ItemStack> esr$inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.esr$inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= 0 && slot < 2) {
            return this.esr$inventory.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(this.esr$inventory, slot, amount);
        if (!result.isEmpty()) {
            this.markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(this.esr$inventory, slot);
        if (!result.isEmpty()) {
            this.markDirty();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= 0 && slot < 2) {
            this.esr$inventory.set(slot, stack);
            if (stack.getCount() > this.getMaxCountPerStack()) {
                stack.setCount(this.getMaxCountPerStack());
            }
            this.markDirty();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        this.esr$inventory.clear();
        this.markDirty();
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void esr$readInventory(NbtCompound nbt, CallbackInfo ci) {
        this.esr$inventory.clear();
        Inventories.readNbt(nbt, this.esr$inventory);
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void esr$writeInventory(NbtCompound nbt, CallbackInfo ci) {
        Inventories.writeNbt(nbt, this.esr$inventory);
    }
}
