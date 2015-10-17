package de.take_weiland.mods.commons.internal.prop;

import de.take_weiland.mods.commons.reflect.PropertyAccess;

import java.util.List;
import java.util.ListIterator;

/**
 * @author diesieben07
 */
public final class ListIteratingProperty<T> extends AbstractIteratingProperty<T, T, ListIterator<T>> {

    private final PropertyAccess<? extends List<T>> listProperty;

    public ListIteratingProperty(PropertyAccess<? extends List<T>> listProperty) {
        this.listProperty = listProperty;
    }

    @Override
    public void start(Object o) {
        it = listProperty.get(o).listIterator();
    }

    @Override
    public void done() {
        it = null; // GC
        curr = null;
    }

    @Override
    public T get(Object o) {
        checkIterating("get");
        return curr;
    }

    @Override
    public void set(Object o, T val) {
        checkIterating("set");
        it.set((curr = val));
    }
}
