package de.take_weiland.mods.commons.function;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface RunnableX {

    static RunnableX of(RunnableX r) {
        return r;
    }

    void run() throws Exception;

    default Runnable catching(Consumer<? super Throwable> handler) {
        return () -> {
            try {
                run();
            } catch (Throwable x) {
                handler.accept(x);
            }
        };
    }

    default Runnable rethrowing(Function<? super Throwable, ? extends RuntimeException> handler) {
        return () -> {
            try {
                run();
            } catch (Throwable x) {
                throw handler.apply(x);
            }
        };
    }

    default Runnable rethrowing() {
        return rethrowing(FuncUtils.checkedRethrower());
    }


    default Runnable sneaky() {
        return rethrowing(FuncUtils.uncheckedRethrower());
    }

    default RunnableX andThen(RunnableX then) {
        return () -> {
            run();
            then.run();
        };
    }

}
