package de.take_weiland.mods.commons.internal;

import com.google.common.collect.*;
import de.take_weiland.mods.commons.serialize.Property;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public abstract class TypeToFactoryMap<F, FR> {

    // lock on "this" guards writes to the map
    // map must be synchronized as long as it's mutable as well,
    // to exclude the "get" call, which does not synchronize on "this".

    private Multimap<Class<?>, F> map = Multimaps.synchronizedMultimap(ArrayListMultimap.<Class<?>, F>create());

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
        map.put(base, factory);
    }

    public final synchronized void freeze() {
        checkNotFrozen();
        map = ImmutableMultimap.copyOf(map);
    }

    public final synchronized boolean isFrozen() {
        return isFrozenNonLocking();
    }

    protected abstract FR applyFactory(F factory, Property<?, ?> type);

    private boolean isFrozenNonLocking() {
        return map instanceof ImmutableMultimap;
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
