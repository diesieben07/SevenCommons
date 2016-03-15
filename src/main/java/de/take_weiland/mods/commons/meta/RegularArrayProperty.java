package de.take_weiland.mods.commons.meta;

import com.google.common.collect.ImmutableMap;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
final class RegularArrayProperty<T> implements MetadataProperty<T> {

    private final ImmutableMap<T, Integer> lookup;
    private final int shift;
    private final int mask;

    RegularArrayProperty(int shift, T[] values) {
        int bitCount = Integer.numberOfTrailingZeros(Integer.highestOneBit(values.length - 1)) + 1;
        checkArgument(shift >= bitCount + 1, "Too many values for given shift (need at least %s)", bitCount - 1);

        this.shift = shift;
        this.mask = (1 << bitCount) - 1;
        ImmutableMap.Builder<T, Integer> b = ImmutableMap.builder();
        for (int i = 0; i < values.length; ++i) {
            b.put(checkNotNull(values[i], "Cannot have null properties!"), i);
        }
        lookup = b.build();
    }

    int toMeta0(T value) {
        Integer i = lookup.get(checkNotNull(value, "value"));
        checkArgument(i != null, "Unknown property %s", value);
        return i;
    }

    @Override
    public final Set<T> values() {
        return lookup.keySet();
    }

    @Override
    public final boolean hasDistinctValues() {
        return true;
    }

    @Override
    public final T value(int metadata) {
        return lookup.keySet().asList().get((metadata >>> shift) & mask);
    }

    @Override
    public final int toMeta(T value, int previousMeta) {
        return (previousMeta & ~(mask << shift)) | (toMeta0(value) << shift);
    }
}
