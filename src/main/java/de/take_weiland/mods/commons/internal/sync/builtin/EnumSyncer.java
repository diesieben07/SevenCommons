package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;

/**
 * @author diesieben07
 */
@SuppressWarnings({"rawtypes", "unchecked"})
final class EnumSyncer implements Syncer.ForImmutable<Enum> {

    private final Class clazz;

    private EnumSyncer(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public Enum decode(MCDataInput in) {
        return in.readEnum(clazz);
    }

    @Override
    public void encode(Enum e, MCDataOutput out) {
        out.writeEnum(e);
    }

    @Override
    public Class<Enum> companionType() {
        return clazz;
    }

    static Syncer<?, ?, ?> get(Class<?> type) {
        //noinspection unchecked,rawtypes
        return BuiltinSyncers.getOrCreateSyncer(type, EnumSyncer::new);
    }
}
