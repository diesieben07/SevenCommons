package de.take_weiland.mods.commons.internal.serialize;

import de.take_weiland.mods.commons.reflect.PropertyAccess;

/**
 * @author diesieben07
 */
public final class DummyProperty<T> implements PropertyAccess<T>  {

    public T value;

    public DummyProperty() {
    }

    public DummyProperty(T value) {
        this.value = value;
    }

    @Override
    public T get(Object o) {
        return value;
    }

    @Override
    public void set(Object o, T val) {
        value = val;
    }
}
