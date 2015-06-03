package de.take_weiland.mods.commons.async;

import java.util.concurrent.Callable;

/**
 * @author diesieben07
 */
public interface Promise<T> {

    static <T> Promise<T> fulfilled(T t) {

    }

    static <T> Promise<T> failed(Throwable t) {

    }

    static <T> Promise<T> of(Callable<? extends T> callable) {

    }

    static Promise<Void> of(RunnableX task) {

    }

    <R> Promise<R> then(FunctionX<? super T, ? extends R> function);

    Promise<Void> then(ConsumerX<? super T> handler);

    <R> Promise<R> catch_(FunctionX<? super Throwable, ? extends R> handler);

    Promise<Void> handle(BiConsumerX<? super T, ? super Throwable> handler);

    <R> Promise<R> handle(BiFunctionX<? super T, ? super Throwable, ? extends R> handler);

}
