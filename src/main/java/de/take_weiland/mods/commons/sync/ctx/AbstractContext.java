package de.take_weiland.mods.commons.sync.ctx;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

/**
 * @author diesieben07
 */
public abstract class AbstractContext<T> implements SyncContext<T> {

    private TypeToken<T> genericType;
    ImmutableMap<SyncContext.Key<?>, Object> data;

    protected abstract TypeToken<T> resolveGenericType();
    protected abstract ImmutableMap<SyncContext.Key<?>, Object> resolveData();

    @SuppressWarnings("unchecked")
    @Override
    public final <V> V getValue(Key<? extends V> key) {
        return (V) getData().get(key);
    }

    @Override
    public final ImmutableMap<Key<?>, Object> getData() {
        if (data == null) {
            data = resolveData();
        }
        return data;
    }

    @Override
    public final TypeToken<T> getGenericType() {
        if (genericType == null) {
            // resolving twice is inefficient but not incorrect (threading)
            genericType = resolveGenericType();
        }
        return genericType;
    }

    @Override
    public boolean equals(Object o) {
        return eq(this, o);
    }

    @Override
    public int hashCode() {
        return hash(this);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("type", getGenericType())
                .add("data", getData())
                .toString();
    }

    static int hash(SyncContext<?> context) {
        return 31 * context.getGenericType().hashCode() + context.getData().hashCode();
    }

    static boolean eq(SyncContext<?> self, Object other) {
        if (self == other) {
            return true;
        } else if (other instanceof SyncContext) {
            SyncContext<?> that = (SyncContext<?>) other;
            return self.getGenericType().equals(that.getGenericType()) && self.getData().equals(that.getData());
        } else {
            return false;
        }
    }
}
