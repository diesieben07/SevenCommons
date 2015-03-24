package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;

/**
 * <p>A factory for a handlers that allow some type T to be used with the {@link de.take_weiland.mods.commons.sync.Sync @Sync}
 * annotation.</p>
 *
 * @author diesieben07
 */
public interface SyncerFactory {

    /**
     * <p>Get a Handle for the given TypeSpecification or null if this factory cannot handle the give type.</p>
     * @param type the TypeSpecification
     * @return a Handle
     */
    Handle get(TypeSpecification<?> type);

    /**
     * <p>A handler for some type.</p>
     */
    public interface Handle {

        /**
         * <p>The type of a potential companion field to allow the "has-changed" check.</p>
         * <p>May be null if no such field is needed, in that case the {@code companionGet} and {@code companionSet}
         * parameters for
         * {@link #make(java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle) make}
         * will be null.</p>
         * @return
         */
        Class<?> getCompanionType();

        /**
         * <p>Provide the functionality necessary to perform the syncing process.</p>
         * <p>This happens in the form of 3 {@code MethodHandles}, provided as a whole in an object of type {@link de.take_weiland.mods.commons.sync.SyncerFactory.Instance}.</p>
         * <p>There are 4 important types here:</p>
         * <ul>
         *     <li>The type of the value being synced, called {@code V}.</li>
         *     <li>The type of the object holding the value {@code V}, called {@code VH}.</li>
         *     <li>The type of the companion field, called {@code C}.</li>
         *     <li>The type of the object holding the companion field, called {@code CH}.</li>
         * </ul>
         * <p>The last 2 types are obviously only relevant if {@link #getCompanionType()} does not return {@code null}.</p>
         * <p>The MethodHandles passed to this method are guaranteed to have the following types:</p>
         * <ul>
         *     <li>{@code V getter(VH)}</li>
         *     <li>{@code void setter(VH, V)}</li>
         *     <li>{@code C companionGet(CH)}</li>
         *     <li>{@code void companionSet(CH, C)}</li>
         * </ul>
         * <p>{@code companionGet} and {@code companionSet} will both be null if {@link #getCompanionType()} returns null.</p>
         * <p>The MethodHandles produced by this method must then have exactly the following types (2nd version denotes the type
         * if no companion field is present ({@link #getCompanionType()} returns null)):</p>
         * <ul>
         *     <li>{@code boolean checker(VH, CH)} / {@code boolean checker(VH)}</li>
         *     <li>{@code void writer(MCDataOutput, VH, CH)} / {@code void writer(MCDataOutput, VH)}</li>
         *     <li>{@code void reader(VH, CH, MCDataInput)} / {@code void reader(VH, MCDataInput)}</li>
         * </ul>
         * @param getter the getter
         * @param setter the setter
         * @param companionGet the companion field getter
         * @param companionSet the companion field setter
         * @return a new {@link de.take_weiland.mods.commons.sync.SyncerFactory.Instance} object
         */
        Instance make(MethodHandle getter, MethodHandle setter, MethodHandle companionGet, MethodHandle companionSet);

    }

    /**
     * <p>Holder class for the result of
     * {@link de.take_weiland.mods.commons.sync.SyncerFactory.Handle#make(java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle) make}.</p>
     */
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
