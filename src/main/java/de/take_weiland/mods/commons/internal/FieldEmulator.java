package de.take_weiland.mods.commons.internal;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author diesieben07
 */
public final class FieldEmulator<T> implements Supplier<T>, Consumer<T> {

    public T val;


    @Override
    public void accept(T t) {
        val = t;
    }

    @Override
    public T get() {
        return val;
    }
}
