package de.take_weiland.mods.commons.inv;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @author diesieben07
 */
final class InventorySpliterator implements Spliterator<ItemStack> {

    private final IInventory inv;
    private int index;
    private final int fence;

    InventorySpliterator(IInventory inv, int index, int fence) {
        this.inv = inv;
        this.index = index;
        this.fence = fence;
    }

    @Override
    public boolean tryAdvance(Consumer<? super ItemStack> action) {
        if (index < fence) {
            action.accept(inv.getStackInSlot(index++));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Spliterator<ItemStack> trySplit() {
        int lo = index, mid = (lo + fence) >>> 1;
        return (lo >= mid)
                ? null
                : new InventorySpliterator(inv, lo, index = mid);
    }

    @Override
    public void forEachRemaining(Consumer<? super ItemStack> action) {
        IInventory in = inv;
        for (int i = index, f = fence; i < f; i++) {
            action.accept(in.getStackInSlot(i));
        }
        index = fence;
    }

    @Override
    public long getExactSizeIfKnown() {
        return fence - index;
    }

    @Override
    public long estimateSize() {
        return fence - index;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE | SIZED | SUBSIZED | ORDERED;
    }
}
