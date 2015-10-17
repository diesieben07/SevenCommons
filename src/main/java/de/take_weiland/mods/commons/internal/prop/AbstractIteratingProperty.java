package de.take_weiland.mods.commons.internal.prop;

import de.take_weiland.mods.commons.reflect.IteratingProperty;

import java.util.Iterator;

/**
 * @author diesieben07
 */
public abstract class AbstractIteratingProperty<T, IT_CONT, IT extends Iterator<? extends IT_CONT>> implements IteratingProperty<T> {

    protected IT_CONT curr;
    protected IT it;

    @Override
    public final boolean moveNext() {
        checkIterating("moveNext");
        if (it.hasNext()) {
            curr = it.next();
            return true;
        } else {
            done();
            return false;
        }
    }

    @Override
    public void done() {
        it = null;
        curr = null;
    }

    protected final void checkIterating(String method) {
        if (it == null) {
            throw newOutsideIterationException(method);
        }
    }

    protected static IllegalStateException newOutsideIterationException(String method) {
        return new IllegalStateException(method + "() called on IteratingProperty outside of iteration!");
    }

}
