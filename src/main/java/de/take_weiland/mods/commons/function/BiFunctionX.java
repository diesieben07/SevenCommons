package de.take_weiland.mods.commons.function;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface BiFunctionX<T, U, R> {

    static <T, U, R> BiFunctionX<T, U, R> of(BiFunctionX<T, U, R> func) {
        return func;
    }

    R apply(T t, U u) throws Exception;

    default BiFunction<T, U, R> catching(Function<? super Throwable, R> handler) {
        return (t, u) -> {
            try {
                return apply(t, u);
            } catch (Throwable x) {
                return handler.apply(x);
            }
        };
    }

    default BiFunction<T, U, R> rethrowing(Function<? super Throwable, ? extends RuntimeException> handler) {
        return (t, u) -> {
            try {
                return apply(t, u);
            } catch (Throwable x) {
                throw handler.apply(x);
            }
        };
    }

    default BiFunction<T, U, R> rethrowing() {
        return rethrowing(FuncUtils.checkedRethrower());
    }


    default BiFunction<T, U, R> sneaky() {
        return rethrowing(FuncUtils.uncheckedRethrower());
    }

    default <V> BiFunctionX<T, U, V> andThen(FunctionX<? super R, ? extends V> then) {
        return (t, u) -> then.apply(apply(t, u));
    }

}
