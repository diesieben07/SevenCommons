package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.nbt.NBTData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

import java.lang.invoke.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class NBTDynCallback {

    public static CallSite makeNBTRead(MethodHandles.Lookup foreignLookup, String name, MethodType type, MethodHandle get, MethodHandle set) throws Throwable {
        if (!type.equals(methodType(void.class, NBTBase.class, Object.class))) {
            throw new IllegalArgumentException("Invalid InDy method type!");
        }

        get = get.asType(methodType(Object.class, Object.class));
        set = set.asType(methodType(void.class, Object.class, Object.class));

        // result type is: void x(NBTBase, Object)

        MethodHandle read = findReadMethod();
        MethodHandle result;
        if (read.type().returnType() == void.class) {
            read = guardContentReader(read);
            // get takes object, returns field value
            // get turns object instance into field value
            // "read" then takes NBT and field value
            result = MethodHandles.filterArguments(read, 1, get);
        } else {
            read = guardValueReader(read);

            // set takes instance and field value, returns void
            // swap params takes field value and instance
            MethodHandle swapParams = MethodHandles.permuteArguments(set, methodType(void.class, Object.class, Object.class), 1, 0);
            // result takes NBT and instance, returns void
            result = MethodHandles.filterArguments(swapParams, 0, read);
        }

        return new ConstantCallSite(result);
    }

    public static CallSite makeNBTWrite(MethodHandles.Lookup foreignLookup, String name, MethodType type, MethodHandle get, MethodHandle set) throws Throwable {
        if (!type.equals(methodType(NBTBase.class, Object.class))) {
            throw new IllegalArgumentException("Invalid InDy method type!");
        }

        get = get.asType(methodType(Object.class, Object.class));

        // write takes Object, returns NBTBase
        MethodHandle write = findWriteMethod();

        return new ConstantCallSite(MethodHandles.filterArguments(write, 0, get));
    }

    private static final MethodHandle IS_NBT_NULL;
    private static final MethodHandle DO_NOTHING;
    private static final MethodHandle NBT_TO_NULL;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            IS_NBT_NULL = lookup.findStatic(NBTData.class, "isSerializedNull",methodType(boolean.class, NBTBase.class));
            DO_NOTHING = lookup.findStatic(NBTDynCallback.class, "doNothing", methodType(void.class, NBTBase.class, Object.class));
            MethodHandle nullConst = MethodHandles.constant(Object.class, null);
            NBT_TO_NULL = MethodHandles.dropArguments(nullConst, 0, NBTBase.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandle guardContentReader(MethodHandle reader) {
        return MethodHandles.guardWithTest(IS_NBT_NULL, DO_NOTHING, reader);
    }

    private static MethodHandle guardValueReader(MethodHandle reader) {
        return MethodHandles.guardWithTest(IS_NBT_NULL, NBT_TO_NULL, reader);
    }

    private static void doNothing(NBTBase nbt, Object o) { }

    private static MethodHandle findReadMethod() throws NoSuchMethodException, IllegalAccessException {
        MethodHandles.Lookup myLookup = MethodHandles.lookup();
        return myLookup.findStatic(NBTDynCallback.class, "read", methodType(void.class, NBTBase.class, Object.class));
    }

    private static MethodHandle findWriteMethod() throws Throwable {
        MethodHandles.Lookup myLookup = MethodHandles.lookup();
        return myLookup.findStatic(NBTDynCallback.class, "write", methodType(NBTBase.class, Object.class));
    }

    private static void read(NBTBase nbt, Object instance) throws Throwable {
        System.out.println("Read called!");
    }

    private static Object read(NBTBase nbt) {
        System.out.println("Read value called!");
        return null;
    }

    private static NBTBase write(Object instance) {
        System.out.println("Write called");
        return new NBTTagString("", "hello");
    }

    private static final Map<Class<?>, MethodHandle> writers = new HashMap<>();


}
