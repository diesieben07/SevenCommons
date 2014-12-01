package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;
import de.take_weiland.mods.commons.sync.impl.EnumSetSyncer;

import java.util.EnumSet;

/**
 * @author diesieben07
 */
public final class EnumSetSyncerFinder implements SyncerFinder {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
        Class<?> enumType = getEnumType(context);
        if (enumType == null) {
            return null;
        }
        return (ValueSyncer<T>) new EnumSetSyncer<>((Class<Enum>) enumType);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
        Class<?> enumType = getEnumType(context);
        if (enumType == null) {
            return null;
        }
        return (ContentSyncer<T>) new EnumSetSyncer.Contents<>((Class<Enum>) enumType);
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
