package de.take_weiland.mods.commons.internal.reflect;

import de.take_weiland.mods.commons.reflect.IllegalAccessorException;

public abstract class ReflectionStrategy {

    protected static void validateInterface(Class<?> iface) {
        if (!iface.isInterface()) {
            throw new IllegalAccessorException("Accessor interface must be an interface");
        }
        if (iface.getInterfaces().length != 0) {
            throw new IllegalAccessorException("Accessor interface must not extend any interfaces");
        }
    }


    public abstract <T> T createAccessor(Class<T> iface);

}
