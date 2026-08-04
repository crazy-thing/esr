package net.crazything.esr.util;

import net.crazything.esr.config.EnchantingSystemReImaginedConfig;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChiseledBookshelfScanner {

    private ChiseledBookshelfScanner() {
    }

    public record GuaranteedSource(BlockPos pos, double distanceSq, Map<Enchantment, Integer> enchantments) {
    }

    public static List<GuaranteedSource> scan(World world, BlockPos tablePos) {
        List<GuaranteedSource> found = new ArrayList<>();

        for (int y = 0; y <= 1; y++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (j == 0 && k == 0) {
                        continue;
                    }
                    if (!world.isAir(tablePos.add(k, y, j))) {
                        continue;
                    }

                    checkPosition(world, tablePos, tablePos.add(k * 2, y, j * 2), found);
                    if (k != 0 && j != 0) {
                        checkPosition(world, tablePos, tablePos.add(k * 2, y, j), found);
                        checkPosition(world, tablePos, tablePos.add(k, y, j * 2), found);
                    }
                }
            }
        }

        String ordering = EnchantingSystemReImaginedConfig.get().guaranteedSlotOrdering;
        if ("NEAREST_FIRST".equals(ordering)) {
            found.sort((a, b) -> Double.compare(a.distanceSq(), b.distanceSq()));
        }
        return found;
    }

    private static void checkPosition(World world, BlockPos tablePos, BlockPos pos,
            List<GuaranteedSource> found) {
        if (!(world.getBlockState(pos).getBlock() instanceof ChiseledBookshelfBlock)) {
            return;
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof ChiseledBookshelfBlockEntity shelf)) {
            return;
        }

        Map<Enchantment, Integer> guaranteed = new LinkedHashMap<>();
        for (int slot = 0; slot < shelf.size(); slot++) {
            ItemStack book = shelf.getStack(slot);
            if (book.isEmpty()) {
                continue;
            }
            Map<Enchantment, Integer> stored = EnchantmentHelper.get(book);
            for (Map.Entry<Enchantment, Integer> entry : stored.entrySet()) {
                guaranteed.merge(entry.getKey(), entry.getValue(), Math::max);
            }
        }

        if (!guaranteed.isEmpty()) {
            double distSq = tablePos.getSquaredDistance(
                    (double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
            found.add(new GuaranteedSource(pos, distSq, guaranteed));
        }
    }

    public static int countRegularBookshelves(World world, BlockPos tablePos) {
        int count = 0;
        for (int y = 0; y <= 1; y++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (j == 0 && k == 0) {
                        continue;
                    }
                    if (!world.isAir(tablePos.add(k, y, j))) {
                        continue;
                    }
                    if (isRegularBookshelf(world, tablePos.add(k * 2, y, j * 2)))
                        count++;
                    if (k != 0 && j != 0) {
                        if (isRegularBookshelf(world, tablePos.add(k * 2, y, j)))
                            count++;
                        if (isRegularBookshelf(world, tablePos.add(k, y, j * 2)))
                            count++;
                    }
                }
            }
        }
        return count;
    }

    private static boolean isRegularBookshelf(World world, BlockPos pos) {
        net.minecraft.block.BlockState state = world.getBlockState(pos);
        return state.isOf(net.minecraft.block.Blocks.BOOKSHELF);
    }

    public static void consumeEnchantment(World world, BlockPos tablePos, Enchantment targetEnchantment) {
        List<GuaranteedSource> sources = scan(world, tablePos);
        for (GuaranteedSource source : sources) {
            BlockEntity be = world.getBlockEntity(source.pos());
            if (be instanceof ChiseledBookshelfBlockEntity shelf) {
                for (int slot = 0; slot < shelf.size(); slot++) {
                    ItemStack book = shelf.getStack(slot);
                    if (!book.isEmpty() && book.isOf(net.minecraft.item.Items.ENCHANTED_BOOK)) {
                        Map<Enchantment, Integer> stored = EnchantmentHelper.get(book);
                        if (stored.containsKey(targetEnchantment)) {
                            shelf.setStack(slot, new ItemStack(net.minecraft.item.Items.BOOK));
                            shelf.markDirty();
                            world.updateNeighbors(source.pos(), world.getBlockState(source.pos()).getBlock());
                            return;
                        }
                    }
                }
            }
        }
    }
}
