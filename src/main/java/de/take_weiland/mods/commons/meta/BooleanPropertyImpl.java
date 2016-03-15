package de.take_weiland.mods.commons.meta;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @author diesieben07
 */
@SuppressWarnings("deprecation")
final class BooleanPropertyImpl implements BooleanProperty, MetadataProperty<Boolean> {

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
    public int toMeta(boolean value, int previousMeta) {
        return value ? previousMeta | mask : previousMeta & ~mask;
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
