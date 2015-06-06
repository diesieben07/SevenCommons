package de.take_weiland.mods.commons.function;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface ConsumerX<T> {

    static <T> ConsumerX<T> of(ConsumerX<T> c) {
        return c;
    }

    void accept(T t) throws Exception;

    default Consumer<T> catching(Consumer<? super Throwable> handler) {
        return t -> {
            try {
                accept(t);
            } catch (Throwable x) {
                handler.accept(x);
            }
        };
    }

    default Consumer<T> rethrowing(Function<? super Throwable, ? extends RuntimeException> handler) {
        return t -> {
            try {
                accept(t);
            } catch (Throwable x) {
                throw handler.apply(x);
            }
        };
    }

    default Consumer<T> rethrowing() {
        return rethrowing(FuncUtils.checkedRethrower());
    }


    default Consumer<T> sneaky() {
        return rethrowing(FuncUtils.uncheckedRethrower());
    }

    default ConsumerX<T> andThen(ConsumerX<? super T> then) {
        return t -> {
            accept(t);
            then.accept(t);
        };
    }

    default <R> ConsumerX<R> onResultOf(FunctionX<? super R, ? extends T> mapper) {
        return r -> accept(mapper.apply(r));
    }

}
