package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class SyncerFactoryUtils {

    public static SyncerFactory.Instance makeSimple(MethodHandle equal, MethodHandle write, MethodHandle read, MethodHandle clone, MethodHandle getter, MethodHandle setter, MethodHandle compGet, MethodHandle compSet) {
        // VT = type of the value
        // V = class that holds the property
        // CT = type of the companion
        // C = class that holds the companion

        // VT getter(V)
        // void setter(VT, V)
        // CT compGet(C)
        // void compSet(CT, C)
        // boolean equal(VT, CT)
        // void write(MCDataOutput, VT)
        // VT read(MCDataInput)
        // CT clone(VT)

        checkArgument(getter.type().parameterCount() == 1 && getter.type().returnType() != void.class, "Invalid getter");
        checkArgument(setter.type().parameterCount() == 2 && setter.type().returnType() == void.class, "Invalid setter");
        checkArgument(compGet.type().parameterCount() == 1 && compGet.type().returnType() != void.class, "Invalid companion getter");
        checkArgument(compSet.type().parameterCount() == 2 && compSet.type().returnType() == void.class, "Invalid companion setter");

        Class<?> valueType = getter.type().returnType();
        Class<?> compType = compGet.type().returnType();
        Class<?> valueHolderClazz = getter.type().parameterType(0);
        Class<?> compHolderClazz = compGet.type().parameterType(0);

        checkArgument(equal.type().equals(methodType(boolean.class, valueType, compType)), "Invalid equality checker");
        checkArgument(write.type().equals(methodType(void.class, MCDataOutput.class, valueType)), "Invalid writer");
        checkArgument(read.type().equals(methodType(valueType, MCDataInput.class)));
        checkArgument(clone == null || clone.type().equals(methodType(compType, valueType)), "Invalid cloner");

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
