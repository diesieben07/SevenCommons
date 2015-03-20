package de.take_weiland.mods.commons.sync;

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

        private final MethodHandle checker;
        private final MethodHandle writer;
        private final MethodHandle reader;

        public Instance(MethodHandle checker, MethodHandle writer, MethodHandle reader) {
            this.checker = checker;
            this.writer = writer;
            this.reader = reader;
        }

        public MethodHandle getChecker() {
            return checker;
        }

        public MethodHandle getWriter() {
            return writer;
        }

        public MethodHandle getReader() {
            return reader;
        }
    }

}
