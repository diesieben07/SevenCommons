package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.SimpleSyncer;
import de.take_weiland.mods.commons.sync.SyncerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author diesieben07
 */
public final class BuiltinSyncers implements SyncerFactory {

    private final Map<Class<?>, SimpleSyncer<?, ?>> cache = new HashMap<>();

    @Override
    public <V, C> SimpleSyncer<V, C> getSyncer(TypeSpecification<V> type) {
        Class<? super V> raw = type.getRawType();

        SimpleSyncer<?, ?> syncer = cache.get(raw);
        if (syncer == null && !cache.containsKey(raw)) {
            syncer = newSyncer(raw);
            cache.put(raw, syncer);
        }
        //noinspection unchecked
        return (SimpleSyncer<V, C>) syncer;
    }

    private static SimpleSyncer<?, ?> newSyncer(Class<?> type) {
        if (type == String.class) {
            return new StringSyncer();
        } else if (type == UUID.class) {
            return new UUIDSyncer();
            // all this boxing below might seem expensive
            // but in the generated companions the Syncer is a constant, therefor all these small methods
            // are likely to be inlined into there, enabling the elemiation of the boxing
        } else if (type == boolean.class) {
            return new BooleanSyncer();
        } else if (type == byte.class) {
            return new ByteSyncer();
        } else if (type == short.class) {
            return new ShortSyncer();
        } else if (type == int.class) {
            return new IntegerSyncer();
        } else if (type == char.class) {
            return new CharSyncer();
        } else if (type == long.class) {
            return new LongSyncer();
        } else if (type == float.class) {
            return new FloatSyncer();
        } else if (type == double.class) {
            return new DoubleSyncer();
        }
        return null;
    }

}