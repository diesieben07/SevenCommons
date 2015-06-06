package de.take_weiland.mods.commons.function;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface BiConsumerX<T, U> {

    static <T, U> BiConsumerX<T, U> of(BiConsumerX<T, U> c) {
        return c;
    }

    void accept(T t, U u) throws Exception;

    default BiConsumer<T, U> catching(Consumer<? super Throwable> handler) {
        return (t, u) -> {
            try {
                accept(t, u);
            } catch (Throwable x) {
                handler.accept(x);
            }
        };
    }

    default BiConsumer<T, U> rethrowing(Function<? super Throwable, ? extends RuntimeException> handler) {
        return catching(ex -> {
            throw handler.apply(ex);
        });
    }

    default BiConsumer<T, U> rethrowing() {
        return rethrowing(FuncUtils.checkedRethrower());
    }

    default BiConsumer<T, U> sneaky() {
        return rethrowing(FuncUtils.uncheckedRethrower());
    }

    default BiConsumerX<T, U> andThen(BiConsumerX<? super T, ? super U> then) {
        return (t, u) -> {
            accept(t, u);
            then.accept(t, u);
        };
    }

}
