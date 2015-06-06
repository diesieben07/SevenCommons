package de.take_weiland.mods.commons.function;

import java.util.function.Function;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface FunctionX<T, R> {

    static <T, R> FunctionX<T, R> of(FunctionX<T, R> f) {
        return f;
    }

    R apply(T t) throws Exception;

    default Function<T, R> catching(Function<? super Throwable, ? extends R> handler) {
        return t -> {
            try {
                return apply(t);
            } catch (Throwable x) {
                return handler.apply(x);
            }
        };
    }

    default Function<T, R> rethrowing(Function<? super Throwable, ? extends RuntimeException> handler) {
        return t -> {
            try {
                return apply(t);
            } catch (Throwable x) {
                throw handler.apply(x);
            }
        };
    }

    default Function<T, R> rethrowing() {
        return rethrowing(FuncUtils.checkedRethrower());
    }


    default Function<T, R> sneaky() {
        return rethrowing(FuncUtils.uncheckedRethrower());
    }

    default <V> FunctionX<T, V> andThen(FunctionX<? super R, ? extends V> then) {
        return t -> then.apply(apply(t));
    }

    default <V> FunctionX<V, R> compose(FunctionX<? super V, ? extends T> before) {
        return v -> apply(before.apply(v));
    }

}
