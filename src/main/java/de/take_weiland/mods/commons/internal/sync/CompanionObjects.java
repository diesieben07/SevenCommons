package de.take_weiland.mods.commons.internal.sync;

import javax.annotation.Nonnull;
import java.lang.invoke.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * <p>Bootstrap class for companion objects for @Sync.</p>
 *
 * @author diesieben07
 */
public final class CompanionObjects {

    private static final ClassValue<MethodHandle> constructors = new ClassValue<MethodHandle>() {
        @Override
        protected MethodHandle computeValue(@Nonnull Class<?> type) {
            return CompanionFactories.makeConstructor(type);
        }
    };

    private static final CallSite cstrCS;

    static {
        try {
            cstrCS = new ConstantCallSite(
                    lookup().findStatic(CompanionObjects.class, "newCompanion", methodType(SyncCompanion.class, Class.class)));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static SyncCompanion newCompanion(Class<?> clazz) throws Throwable {
        return (SyncCompanion) constructors.get(clazz).invokeExact();
    }

    public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType methodType) {
        checkArgument(methodType.equals(methodType(SyncCompanion.class, Class.class)));
        return cstrCS;
    }

}
