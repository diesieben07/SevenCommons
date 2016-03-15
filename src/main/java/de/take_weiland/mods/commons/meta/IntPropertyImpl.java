package de.take_weiland.mods.commons.meta;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Set;

/**
 * @author diesieben07
 */
@SuppressWarnings("deprecation")
final class IntPropertyImpl implements IntProperty, MetadataProperty<Integer> {

    private final int shift;
    private final int mask;
    private final Set<Integer> values;

    IntPropertyImpl(int shift, int bits) {
        this.shift = shift;
        this.mask = (1 << bits) - 1;
        values = ContiguousSet.create(Range.closedOpen(0, 1 << bits), DiscreteDomain.integers());
    }

    @Override
    public int intValue(int metadata) {
        return (metadata >> shift) & mask;
    }

    @Override
    public int intValue(World world, int x, int y, int z) {
        return intValue(world.getBlockMetadata(x, y, z));
    }

    @Override
    public int intValue(ItemStack stack) {
        return intValue(stack.getMetadata());
    }

    @Override
    public int toMeta(int value, int previousMeta) {
        return (previousMeta & ~(mask << shift)) | ((value & mask) << shift);
    }

    @Override
    public ItemStack apply(int value, ItemStack stack) {
        stack.setMetadata(toMeta(value, stack.getMetadata()));
        return stack;
    }

    @Override
    public void apply(int value, World world, int x, int y, int z) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), 3);
    }

    @Override
    public void apply(int value, World world, int x, int y, int z, int notifyFlags) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), notifyFlags);
    }

    @Override
    public Integer value(int metadata) {
        return intValue(metadata);
    }

    @Override
    public int toMeta(Integer value, int previousMeta) {
        return toMeta(value.intValue(), previousMeta);
    }

    @Override
    public Set<Integer> values() {
        return values;
    }

    @Override
    public boolean hasDistinctValues() {
        return false;
    }
}
