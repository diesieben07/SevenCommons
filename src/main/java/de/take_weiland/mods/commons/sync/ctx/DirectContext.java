package de.take_weiland.mods.commons.sync.ctx;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
public final class DirectContext<T> implements SyncContext<T> {

    private final Type type;
    private final ImmutableMap<Key<?>, Object> data;
    private TypeToken<T> genericType;

    public DirectContext(Type type, ImmutableMap<Key<?>, Object> data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public ImmutableMap<Key<?>, Object> getData() {
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V getValue(Key<? extends V> key) {
        return (V) data.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeToken<T> getGenericType() {
        return genericType == null ? (genericType = (TypeToken<T>) TypeToken.of(type)) : genericType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? super T> getRawType() {
        return (Class<? super T>) (type instanceof Class ? type : getGenericType().getRawType());
    }

}
