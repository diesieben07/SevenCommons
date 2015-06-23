package de.take_weiland.mods.commons.net;

import com.google.common.collect.UnmodifiableIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author diesieben07
 */
final class OnePlusIterator<T> extends UnmodifiableIterator<T> {

    Iterator<? extends T> it;
    final T extra;

    OnePlusIterator(Iterator<? extends T> it, T extra) {
        this.it = it;
        this.extra = extra;
    }

    @Override
    public boolean hasNext() {
        return it != null;
    }

    @Override
    public T next() {
        if (it == null) {
            throw new NoSuchElementException();
        }
        if (!it.hasNext()) {
            it = null;
            return extra;
        } else {
            return it.next();
        }
    }
}
