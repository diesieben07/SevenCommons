package de.take_weiland.mods.commons.syncx;

import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;

/**
 * <p></p>
 *
 * @author diesieben07
 */
public interface SyncerFactory {

    Handle get(TypeSpecification<?> type);

    public interface Handle {

        Class<?> getCompanionType();

        Instance make(MethodHandle getter, MethodHandle setter, MethodHandle companionGet, MethodHandle companionSet);

    }

    final class Instance {

        final MethodHandle checker;
        final MethodHandle writer;
        final MethodHandle reader;

        public Instance(MethodHandle checker, MethodHandle writer, MethodHandle reader) {
            this.checker = checker;
            this.writer = writer;
            this.reader = reader;
        }
    }

}
