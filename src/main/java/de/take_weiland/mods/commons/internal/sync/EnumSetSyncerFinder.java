package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;
import de.take_weiland.mods.commons.sync.impl.EnumSetSyncer;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentMap;

/**
 * @author diesieben07
 */
public final class EnumSetSyncerFinder implements SyncerFinder {

    private static ConcurrentMap<Class<?>, EnumSetSyncer<?>> cacheValue;
    private static ConcurrentMap<Class<?>, EnumSetSyncer.Contents<?>> cacheContents;

    static {
        MapMaker mm = new MapMaker().concurrencyLevel(2);
        cacheValue = mm.makeMap();
        cacheContents = mm.makeMap();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
        Class<?> enumType = getEnumType(context);
        if (enumType == null) {
            return null;
        }

        ConcurrentMap<Class<?>, EnumSetSyncer<?>> cache = cacheValue;
        EnumSetSyncer<?> syncer;

        if (cache == null) {
            syncer = new EnumSetSyncer<>((Class<Enum>) enumType);
        } else {
            syncer = cache.get(enumType);
            if (syncer == null) {
                syncer = new EnumSetSyncer<>((Class<Enum>) enumType);
                if (cache.putIfAbsent(enumType, syncer) != null) {
                    syncer = cache.get(enumType);
                }
            }
        }
        return (ValueSyncer<T>) syncer;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
        Class<?> enumType = getEnumType(context);
        if (enumType == null) {
            return null;
        }

        ConcurrentMap<Class<?>, EnumSetSyncer.Contents<?>> cache = cacheContents;
        EnumSetSyncer.Contents<?> syncer;

        if (cache == null) {
            syncer = new EnumSetSyncer.Contents<>((Class<Enum>) enumType);
        } else {
            syncer = cache.get(enumType);
            if (syncer == null) {
                syncer = new EnumSetSyncer.Contents<>((Class<Enum>) enumType);
                if (cache.putIfAbsent(enumType, syncer) != null) {
                    syncer = cache.get(enumType);
                }
            }
        }
        return (ContentSyncer<T>) syncer;
    }

    private static Class<?> getEnumType(SyncContext<?> context) {
        if (context.getRawType() != EnumSet.class) {
            return null;
        } else {
            Class<?> enumType = context.getGenericType().resolveType(EnumSet.class.getTypeParameters()[0]).getRawType();
            if (!enumType.isEnum()) {
                throw new RuntimeException("Could not determine Enum type of EnumSet " + context);
            } else {
                return enumType;
            }
        }
    }
}
