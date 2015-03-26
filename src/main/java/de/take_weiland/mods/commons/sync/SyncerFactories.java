package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;

/**
 * <p>Utilities for implementing a {@link SyncerFactory}.</p>
 *
 * @author diesieben07
 */
public final class SyncerFactories {

    /**
     * <p>Create a standard set of MethodHandles for syncing based on the following (the same type names are used as in the
     * documentation for
     * {@link de.take_weiland.mods.commons.sync.SyncerFactory.Handle#make(MethodHandle, MethodHandle, MethodHandle, MethodHandle) SyncerFactory.Handle.make()}:</p>
     * <ul>
     *     <li>{@code boolean equal(V, C)}</li>
     *     <li>{@code void write(MCDataOutput, V)}</li>
     *     <li>{@code V read(MCDataInput)}</li>
     *     <li>{@code C clone(V)}</li>
     *     <li>{@code V getter(VH)}</li>
     *     <li>{@code void setter(VH, V)}</li>
     *     <li>{@code C companionGetter(CH)}</li>
     *     <li>{@code void companionSetter(CH, C}</li>
     * </ul>
     * <p>This method produces code equivalent to the following:</p>
     * <code><pre>
     *     boolean checker(VH vh, CH ch) {
     *         return equal(getter(vh), companionGetter(ch));
     *     }
     *
     *     void writer(MCDataOutput out, VH vh, CH ch) {
     *         V v = getter(vh);
     *         write(out, v);
     *         companionSetter(ch, clone(v));
     *     }
     *
     *     void reader(VH vh, CH ch, MCDataInput in) {
     *         setter(vh, read(in));
     *     }
     * </pre></code>
     * @param equal equality checker
     * @param write writer method
     * @param read reader method
     * @param clone cloner method, may be null
     * @param getter getter for property
     * @param setter setter for property
     * @param companionGetter getter for companion field
     * @param companionSetter setter for companion field
     * @return
     */
    public static SyncerFactory.Instance makeSimple(MethodHandle equal, MethodHandle write, MethodHandle read, MethodHandle clone, MethodHandle getter, MethodHandle setter, MethodHandle companionGetter, MethodHandle companionSetter) {
        checkArgument(getter.type().parameterCount() == 1 && getter.type().returnType() != void.class, "Invalid getter");
        checkArgument(setter.type().parameterCount() == 2 && setter.type().returnType() == void.class, "Invalid setter");
        checkArgument(companionGetter.type().parameterCount() == 1 && companionGetter.type().returnType() != void.class, "Invalid companion getter");
        checkArgument(companionSetter.type().parameterCount() == 2 && companionSetter.type().returnType() == void.class, "Invalid companion setter");

        Class<?> valueType = getter.type().returnType();
        Class<?> compType = companionGetter.type().returnType();
        Class<?> valueHolderClazz = getter.type().parameterType(0);
        Class<?> compHolderClazz = companionGetter.type().parameterType(0);

        checkArgument(equal.type().equals(methodType(boolean.class, valueType, compType)), "Invalid equality checker");
        checkArgument(write.type().equals(methodType(void.class, MCDataOutput.class, valueType)), "Invalid writer");
        checkArgument(read.type().equals(methodType(valueType, MCDataInput.class)));
        checkArgument(clone == null || clone.type().equals(methodType(compType, valueType)), "Invalid cloner");
        if (clone == null) {
            checkArgument(compType == valueType, "no cloner needs companionType==valueType");
        }

        MethodHandle checker = MethodHandles.filterArguments(equal, 0, getter, companionGetter);

        MethodHandle transCompSet;
        if (clone == null) {
            transCompSet = companionSetter;
        } else {
            transCompSet = MethodHandles.filterArguments(companionSetter, 1, clone);
        }
        MethodHandle droppingCompSet = MethodHandles.permuteArguments(transCompSet, methodType(void.class, MCDataOutput.class, valueType, compHolderClazz), 2, 1);

        MethodHandle droppingWrite = MethodHandles.dropArguments(write, 2, compHolderClazz);
        MethodHandle setAndWrite = MethodHandles.foldArguments(droppingWrite, droppingCompSet);

        MethodHandle writer = MethodHandles.filterArguments(setAndWrite, 1, getter);

        MethodHandle droppingSet = MethodHandles.dropArguments(setter, 1, compHolderClazz);
        MethodHandle reader = MethodHandles.filterArguments(droppingSet, 2, read);
        return new SyncerFactory.Instance(checker, writer, reader);
    }

    private SyncerFactories() { }

}
