package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.ImmutableMap;

import java.lang.invoke.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

/**
 * <p>Bootstrap class for companion objects for @Sync.</p>
 *
 * @author diesieben07
 */
public final class CompanionObjects {

    private static MethodHandle getCompanion;
    private static MethodHandle constNull;
    private static MethodHandle getCompanionBound;
    private static final SwitchPoint sp = new SwitchPoint();

    private static ImmutableMap<Class<?>, MethodHandle> cstrs;

    /**
     * <p>Builds all necessary companions, must be called when </p>
     */
    public static synchronized void grabAnnotationData() {
        cstrs = CompanionGenerators.buildAllConstructors();

        SwitchPoint.invalidateAll(new SwitchPoint[] { sp });
    }

    public synchronized static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType methodType) {
        checkArgument(methodType.equals(methodType(SyncCompanion.class, Class.class)));

        // need to take care of the rare case that someone might try to construct
        // a Synced object before FMLConstructionEvent (we need the ASMDataTable from there)
        // if that happens (should never happen, really) just return null for the companion
        // until the ASMData is present
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
                            "getCompFromMap", methodType(SyncCompanion.class, ImmutableMap.class, Class.class))
                        .bindTo(cstrs);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw newInternalError(e);
            }
        }
        return getCompanionBound;
    }

    private static MethodHandle getConstNull() {
        if (constNull == null) {
            constNull = dropArguments(constant(SyncCompanion.class, null), 0, Class.class);
        }
        return constNull;
    }

    private static MethodHandle getGetCompanion() {
        if (getCompanion == null) {
            try {
                getCompanion = lookup().findStatic(CompanionObjects.class, "getCompanion", methodType(SyncCompanion.class, Class.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw newInternalError(e);
            }
        }
        return getCompanion;
    }

    private static SyncCompanion getCompFromMap(ImmutableMap<Class<?>, MethodHandle> map, Class<?> clazz) throws Throwable {
        MethodHandle mh = map.get(clazz);
        return mh == null ? null : (SyncCompanion) mh.invokeExact();
    }

    private static RuntimeException newInternalError(Throwable t) {
        throw new RuntimeException("Impossible", t);
    }

    public static SyncCompanion getCompanion(Class<?> clazz) throws Throwable {
        return getCompFromMap(cstrs, clazz);
    }

}
