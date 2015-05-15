package de.take_weiland.mods.commons.internal.sync;

import java.lang.invoke.MethodHandle;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class PropertySupport {

    public static Function<Object, Object> makeGetter(MethodHandle getter) {
        MethodHandle withRawTypes = getter.asType(methodType(Object.class, Object.class));
        return o -> {
            try {
                return withRawTypes.invokeExact(o);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }

    public static BiConsumer<Object, Object> makeSetter(MethodHandle setter) {
        MethodHandle withRawTypes = setter.asType(methodType(void.class, Object.class, Object.class));
        return (o, o2) -> {
            try {
                withRawTypes.invokeExact(o, o2);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }

    public static Function<Object, Object> makeCompanionGetter(MethodHandle getter) {
        MethodHandle withRawTypes = getter.asType(methodType(Object.class, Object.class));
        return o -> {
            Object companion = ((SyncedObjectProxy) o)._sc$getCompanion();
            try {
                return withRawTypes.invokeExact(companion);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }

    public static BiConsumer<Object, Object> makeCompanionSetter(MethodHandle setter) {
        MethodHandle withRawTypes = setter.asType(methodType(void.class, Object.class, Object.class));
        return (o, o2) -> {
            Object companion = ((SyncedObjectProxy) o)._sc$getCompanion();
            try {
                withRawTypes.invokeExact(companion, o2);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }

    private PropertySupport() {
    }

}
