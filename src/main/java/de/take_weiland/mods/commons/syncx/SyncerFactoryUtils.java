package de.take_weiland.mods.commons.syncx;

import de.take_weiland.mods.commons.net.MCDataOutput;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class SyncerFactoryUtils {

    public static SyncerFactory.Instance makeSimple(MethodHandle equal, MethodHandle write, MethodHandle read, MethodHandle clone, MethodHandle getter, MethodHandle setter, MethodHandle compGet, MethodHandle compSet) {
        Class<?> valueType = getter.type().returnType();
        Class<?> compType = compGet.type().returnType();
        Class<?> valueHolderClazz = getter.type().parameterType(0);
        Class<?> compHolderClazz = compGet.type().parameterType(0);

        // TODO validation

        MethodHandle checker = MethodHandles.filterArguments(equal, 0, getter, compGet);

        MethodHandle transCompSet;
        if (clone == null) {
            transCompSet = compSet;
        } else {
            transCompSet = MethodHandles.filterArguments(compSet, 1, clone);
        }
        MethodHandle droppingCompSet = MethodHandles.permuteArguments(transCompSet, methodType(void.class, MCDataOutput.class, valueType, compHolderClazz), 2, 1);

        MethodHandle droppingWrite = MethodHandles.dropArguments(write, 2, compHolderClazz);
        MethodHandle setAndWrite = MethodHandles.foldArguments(droppingWrite, droppingCompSet);

        MethodHandle writer = MethodHandles.filterArguments(setAndWrite, 1, getter);

        MethodHandle droppingSet = MethodHandles.dropArguments(setter, 1, compHolderClazz);
        MethodHandle reader = MethodHandles.filterArguments(droppingSet, 2, read);
        return new SyncerFactory.Instance(checker, writer, reader);
    }

}
