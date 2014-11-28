package de.take_weiland.mods.commons.sync.ctx;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

/**
 * @author diesieben07
 */
public final class ArrayElementContext<T> extends AbstractContext<T> {

    private final SyncContext<T[]> arrayContext;

    public ArrayElementContext(SyncContext<T[]> arrayContext) {
        this.arrayContext = arrayContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? super T> getRawType() {
        return (Class<? super T>) arrayContext.getRawType().getComponentType();
    }

    @Override
    protected ImmutableMap<Key<?>, Object> resolveData() {
        return arrayContext.getData();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TypeToken<T> resolveGenericType() {
        return (TypeToken<T>) arrayContext.getGenericType().getComponentType();
    }
}
