package de.take_weiland.mods.commons.sync.ctx;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author diesieben07
 */
public final class DirectContext<T> implements SyncContext<T> {

    private final Type type;
    private final ImmutableMap<Key<?>, Object> data;
    private TypeToken<T> genericType;

    public DirectContext(Class<T> clazz) {
        this((Type) clazz);
    }

    public DirectContext(Type type) {
        this(type, ImmutableMap.<Key<?>, Object>of());
    }

    public DirectContext(Class<T> clazz, Map<Key<?>, ?> data) {
        this((Type) clazz, data);
    }

    public DirectContext(Type type, Map<Key<?>, ?> data) {
        this.type = type;
        this.data = ImmutableMap.copyOf(data);
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

    @Override
    public int hashCode() {
        return AbstractContext.hash(this);
    }

    @Override
    public boolean equals(Object obj) {
        return AbstractContext.eq(this, obj);
    }
}
