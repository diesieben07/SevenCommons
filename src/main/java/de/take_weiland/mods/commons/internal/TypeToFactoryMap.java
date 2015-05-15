package de.take_weiland.mods.commons.internal;

import com.google.common.collect.*;
import de.take_weiland.mods.commons.serialize.Property;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public abstract class TypeToFactoryMap<F, FR> {

    // lock on "this" guards writes to the map
    // map must be synchronized as long as it's mutable as well,
    // to exclude the "get" call, which does not synchronize on "this".

    private Map<Class<?>, Collection<F>> map = new ConcurrentHashMap<>();

    public final FR get(Property<?, ?> type) {
        Class<?> rawType = type.getRawType();
        Iterable<Class<?>> hierarchy;
        if (rawType.isPrimitive()) {
            hierarchy = Arrays.asList(rawType, Object.class);
        } else if (rawType.isInterface()) {
            hierarchy = Iterables.concat(
                    JavaUtils.hierarchy(rawType, JavaUtils.Interfaces.INCLUDE),
                    Collections.singleton(Object.class)
            );
        } else {
            hierarchy = JavaUtils.hierarchy(rawType, JavaUtils.Interfaces.INCLUDE);
        }

        for (Class<?> baseClass : hierarchy) {
            FR result = resultFromList(type, map.get(baseClass));
            if (result != null) {
                return result;
            }
        }
        throw new IllegalStateException("No applicable factory found for type " + type);
    }

    public final synchronized void register(Class<?> base, F factory) {
        checkNotFrozen();
        map.computeIfAbsent(base, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(factory);
    }

    public final synchronized void freeze() {
        checkNotFrozen();
        ImmutableMap.Builder<Class<?>, Collection<F>> builder = ImmutableMap.builder();
        for (Map.Entry<Class<?>, Collection<F>> entry : map.entrySet()) {
            builder.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
        }
        map = builder.build();
    }

    protected abstract FR applyFactory(F factory, Property<?, ?> type);

    private boolean isFrozenNonLocking() {
        return map instanceof ImmutableMap;
    }

    private void checkNotFrozen() {
        checkState(!isFrozenNonLocking(), "Map frozen");
    }

    private FR resultFromList(Property<?, ?> type, Collection<F> list) {
        FR result = null;
        for (F factory : list) {
            if (result == null) {
                result = applyFactory(factory, type);
            } else if (applyFactory(factory, type) != null) {
                throw new IllegalStateException("Multiple factories applicable for type " + type);
            }
        }
        return result;
    }

}
