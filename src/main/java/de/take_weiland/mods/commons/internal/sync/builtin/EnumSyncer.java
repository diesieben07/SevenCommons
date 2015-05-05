package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;

/**
 * @author diesieben07
 */
final class EnumSyncer<E extends Enum<E>> implements Syncer.ForImmutable<E> {

    private final Class<E> clazz;

    EnumSyncer(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<E> getCompanionType() {
        return clazz;
    }

    @Override
    public void write(E value, MCDataOutput out) {
        out.writeEnum(value);
    }

    @Override
    public E read(MCDataInput in) {
        return in.readEnum(clazz);
    }
}
