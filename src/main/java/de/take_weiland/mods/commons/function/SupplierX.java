package de.take_weiland.mods.commons.function;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface SupplierX<T> extends Callable<T> {

    static <T> SupplierX<T> of(SupplierX<T> s) {
        return s;
    }

    static <T> SupplierX<T> of(Callable<T> c) {
        return c instanceof SupplierX ? ((SupplierX<T>) c) : c::call;
    }

    T apply() throws Exception;

    @Override
    default T call() throws Exception {
        return apply();
    }

    default Supplier<T> catching(Function<? super Throwable, ? extends T> handler) {
        return () -> {
            try {
                return apply();
            } catch (Throwable x) {
                return handler.apply(x);
            }
        };
    }

    default Supplier<T> rethrowing(Function<? super Throwable, ? extends RuntimeException> handler) {
        return () -> {
            try {
                return apply();
            } catch (Throwable x) {
                throw handler.apply(x);
            }
        };
    }

    default Supplier<T> rethrowing() {
        return rethrowing(FuncUtils.checkedRethrower());
    }


    default Supplier<T> sneaky() {
        return rethrowing(FuncUtils.uncheckedRethrower());
    }

    default <V> SupplierX<V> andThen(FunctionX<? super T, ? extends V> mapper) {
        return () -> mapper.apply(apply());
    }

}
