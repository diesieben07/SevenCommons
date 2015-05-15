package de.take_weiland.mods.commons.meta;

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Set;

/**
 * @author diesieben07
 */
final class BooleanPropertyImpl extends GenericProperty<Boolean> implements BooleanProperty {

    private static final BooleanPropertyImpl[] cache = new BooleanPropertyImpl[32];

    static synchronized BooleanPropertyImpl get(int shift) {
        if (cache[shift] == null) {
            cache[shift] = new BooleanPropertyImpl(shift);
        }
        return cache[shift];
    }

    private final int mask;

    private BooleanPropertyImpl(int shift) {
        mask = 1 << shift;
    }

    @Override
    public boolean booleanValue(int metadata) {
        return (metadata & mask) != 0;
    }

    @Override
    public boolean booleanValue(ItemStack stack) {
        return booleanValue(stack.getItemDamage());
    }

    @Override
    public boolean booleanValue(World world, int x, int y, int z) {
        return booleanValue(world.getBlockMetadata(x, y, z));
    }

    @Override
    public int toMeta(boolean value, int previousMeta) {
        return value ? previousMeta | mask : previousMeta;
    }

    @Override
    public ItemStack apply(boolean value, ItemStack stack) {
        stack.setItemDamage(toMeta(value, stack.getItemDamage()));
        return stack;
    }

    @Override
    public void apply(boolean value, World world, int x, int y, int z) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), 3);
    }

    @Override
    public void apply(boolean value, World world, int x, int y, int z, int notifyFlags) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), notifyFlags);
    }

    @Override
    public Boolean value(int metadata) {
        return booleanValue(metadata);
    }

    @Override
    public int toMeta(Boolean value, int previousMeta) {
        return toMeta(value.booleanValue(), previousMeta);
    }

    private static final Set<Boolean> values = ImmutableSet.of(Boolean.FALSE, Boolean.TRUE);

    @Override
    public Set<Boolean> values() {
        return values;
    }

    @Override
    public boolean hasDistinctValues() {
        return true;
    }
}
