package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;

import java.util.function.Consumer;

/**
 * @author diesieben07
 */
final class EnumSyncer<E extends Enum<E>> implements Syncer.Simple<E, E> {

    private final Class<E> clazz;

    EnumSyncer(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<E> getCompanionType() {
        return clazz;
    }

    @Override
    public <T_OBJ> Change<E> checkChange(T_OBJ obj, E value, E companion, Consumer<E> companionSetter) {
        if (value == companion) {
            return noChange();
        } else {
            companionSetter.accept(value);
            return newValue(value);
        }
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
