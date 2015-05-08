package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.AbstractSyncer;
import de.take_weiland.mods.commons.sync.PropertyAccess;

/**
 * @author diesieben07
 */
final class EnumSyncer<E extends Enum<E>> extends AbstractSyncer.ForImmutable<E> {

    private final Class<E> clazz;

    protected <OBJ> EnumSyncer(OBJ obj, PropertyAccess<OBJ, E> property) {
        super(obj, property);
        this.clazz = property.getType();
    }

    @Override
    protected E decode(MCDataInput in) {
        return in.readEnum(clazz);
    }

    @Override
    public void encode(E e, MCDataOutput out) {
        out.writeEnum(e);
    }
}
