package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.ImmutableMap;

import java.lang.invoke.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class CompanionObjects {

    private static MethodHandle getCompanion;
    private static MethodHandle constNull;
    private static MethodHandle getCompanionBound;
    private static final SwitchPoint sp = new SwitchPoint();

    private static ImmutableMap<Class<?>, MethodHandle> cstrs;

    public static synchronized void grabAnnotationData() {
        cstrs = CompanionGenerators.buildAllConstructors();

        SwitchPoint.invalidateAll(new SwitchPoint[] { sp });
    }

    public synchronized static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType methodType) {
        checkArgument(methodType.equals(methodType(SyncerCompanion.class, Class.class)));

        MethodHandle target;
        if (sp.hasBeenInvalidated()) {
            target = getBoundCompanionGet();
        } else {
            target = sp.guardWithTest(getConstNull(), getGetCompanion());
        }
        return new ConstantCallSite(target);
    }

    private static MethodHandle getBoundCompanionGet() {
        if (getCompanionBound == null) {
            try {
                getCompanionBound = lookup().findStatic(CompanionObjects.class,
                            "getCompFromMap", methodType(SyncerCompanion.class, ImmutableMap.class, Class.class))
                        .bindTo(cstrs);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw newInternalError(e);
            }
        }
        return getCompanionBound;
    }

    private static MethodHandle getConstNull() {
        if (constNull == null) {
            constNull = dropArguments(constant(SyncerCompanion.class, null), 0, Class.class);
        }
        return constNull;
    }

    private static MethodHandle getGetCompanion() {
        if (getCompanion == null) {
            try {
                getCompanion = lookup().findStatic(CompanionObjects.class, "getCompanion", methodType(SyncerCompanion.class, Class.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw newInternalError(e);
            }
        }
        return getCompanion;
    }

    private static SyncerCompanion getCompFromMap(ImmutableMap<Class<?>, MethodHandle> map, Class<?> clazz) throws Throwable {
        MethodHandle mh = map.get(clazz);
        return mh == null ? null : (SyncerCompanion) mh.invokeExact();
    }

    private static RuntimeException newInternalError(Throwable t) {
        throw new RuntimeException("Impossible", t);
    }

    private static SyncerCompanion getCompanion(Class<?> clazz) throws Throwable {
        return getCompFromMap(cstrs, clazz);
    }

}
