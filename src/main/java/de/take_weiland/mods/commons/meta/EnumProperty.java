package de.take_weiland.mods.commons.meta;

import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.util.EnumUtils;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * @author diesieben07
 */
final class EnumProperty<T extends Enum<T>> implements MetadataProperty<T> {

    private final int shift;
    private final int mask;
    private final Class<T> clazz;
    private final ImmutableSet<T> values;

    EnumProperty(int shift, Class<T> clazz) {
        this.shift = shift;
        this.clazz = clazz;
        this.values = ImmutableSet.copyOf(EnumSet.allOf(clazz));
        // values.size - 1 is largest ordinal ever used
        // highestOneBit of that << 1 is bit right above the MSB of the largest ordinal value
        // that - 1 gives a mask for all values
        this.mask = (Integer.highestOneBit(values.size() - 1) << 1) - 1;
    }

    @Override
    public T value(int metadata) {
        return EnumUtils.byOrdinal(clazz, (metadata >>> shift) & mask);
    }

    @Override
    public int toMeta(T value, int previousMeta) {
        return (previousMeta & ~(mask << shift)) | value.ordinal() << shift;
    }

    @Override
    public boolean hasDistinctValues() {
        return true;
    }

    @Override
    public Set<T> values() {
        return values;
    }

    @Override
    public <V> Map<T, V> createMap() {
        return new EnumMap<>(clazz);
    }
}
