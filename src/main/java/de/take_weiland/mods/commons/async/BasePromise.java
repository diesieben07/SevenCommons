package de.take_weiland.mods.commons.async;

/**
 * @author diesieben07
 */
abstract class BasePromise<T> implements Promise<T> {

    abstract void addListener(BiConsumerX<? super T, ? super Throwable> handler);

    @Override
    public <R> Promise<R> then(FunctionX<? super T, ? extends R> function) {
        return null;
    }
}
