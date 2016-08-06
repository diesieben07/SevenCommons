package de.take_weiland.mods.commons.internal.sync_olds.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.TypeSyncer;

/**
 * @author diesieben07
 */
final class EnumSyncer<E extends Enum<E>> implements TypeSyncer.ForImmutable<E> {

    private final Class<E> clazz;

    private EnumSyncer(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public E decode(MCDataInput in) {
        return in.readEnum(clazz);
    }

    @Override
    public void encode(E e, MCDataOutput out) {
        out.writeEnum(e);
    }

    @Override
    public Class<E> companionType() {
        return clazz;
    }

    static TypeSyncer<?, ?, ?> get(Class<?> type) {
        //noinspection unchecked,rawtypes
        return BuiltinSyncers.getOrCreateSyncer(type, clazz -> new EnumSyncer<>((Class) clazz));
    }
}
