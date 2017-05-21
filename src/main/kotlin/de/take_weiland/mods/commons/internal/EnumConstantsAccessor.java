package de.take_weiland.mods.commons.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class EnumConstantsAccessor {

    private static final MethodHandle accessEnumConstants;

    static {
        MethodHandle mh;
        Method m;
        try {
            m = Class.class.getDeclaredMethod("getEnumConstantsShared");
            m.setAccessible(true);
            mh = MethodHandles.publicLookup().unreflect(m).asType(methodType(Object[].class, Class.class));
        } catch (ReflectiveOperationException e) {
            ClassValue<Object[]> cv = new ClassValue<Object[]>() {
                @Override
                protected Object[] computeValue(Class<?> type) {
                    return type.getEnumConstants();
                }
            };
            try {
                mh = MethodHandles.publicLookup().findVirtual(ClassValue.class, "get", methodType(Object.class, Class.class))
                        .bindTo(cv)
                        .asType(methodType(Object[].class, Class.class));
            } catch (NoSuchMethodException | IllegalAccessException e1) {
                throw new RuntimeException(e1);
            }
        }

        accessEnumConstants = mh;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> cls) throws Throwable {
        return (E[]) (Object[]) accessEnumConstants.invokeExact((Class<?>) cls);
    }

}
