package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;

/**
 * @author diesieben07
 */
final class EnumSyncer<E extends Enum<E>> implements Syncer<E, E> {

    private final Class<E> clazz;

    EnumSyncer(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<E> getCompanionType() {
        return clazz;
    }

    @Override
    public boolean equal(E value, E companion) {
        return value == companion;
    }

    @Override
    public E writeAndUpdate(E value, E companion, MCDataOutput out) {
        out.writeEnum(value);
        return value;
    }

    @Override
    public E read(E value, E companion, MCDataInput in) {
        return in.readEnum(clazz);
    }
}
