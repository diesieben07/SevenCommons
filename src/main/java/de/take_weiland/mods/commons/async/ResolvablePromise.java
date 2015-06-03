package de.take_weiland.mods.commons.async;

/**
 * @author diesieben07
 */
class ResolvablePromise<T> extends BasePromise<T> {


    @Override
    void addListener(BiConsumerX<? super T, ? super Throwable> handler) {

    }

    @Override
    public Promise<Void> then(ConsumerX<? super T> handler) {
        return null;
    }

    @Override
    public <R> Promise<R> catch_(FunctionX<? super Throwable, ? extends R> handler) {
        return null;
    }

    @Override
    public Promise<Void> handle(BiConsumerX<? super T, ? super Throwable> handler) {
        return null;
    }

    @Override
    public <R> Promise<R> handle(BiFunctionX<? super T, ? super Throwable, ? extends R> handler) {
        return null;
    }
}
