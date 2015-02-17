package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.nbt.NBTSerializers;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import net.minecraft.nbt.NBTBase;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_CLASS_ARRAY;

/**
 * @author diesieben07
 */
public final class ToNbtBootstrap {

    public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type, MethodHandle getter, MethodHandle setter, String memberName, int memberIsMethod) throws Throwable {
        Class<?> callingClass = lookup.lookupClass();
        Class<?> propertyType = getter.type().returnType();

        checkArgument(getter.type().equals(methodType(propertyType, callingClass)));
        checkArgument(setter.type().equals(methodType(void.class, callingClass, propertyType)));

        TypeSpecification<?> typeSpec;
        if (memberIsMethod > 0) {
            typeSpec = new MethodTypeSpec<>(callingClass.getDeclaredMethod(memberName, EMPTY_CLASS_ARRAY));
        } else {
            typeSpec = new FieldTypeSpec<>(callingClass.getDeclaredField(memberName));
        }

        if (name.equals("read")) {
            checkArgument(type.equals(methodType(void.class, callingClass, NBTBase.class)));
            return NBTSerializers.makeReaderCallSite(typeSpec, getter, setter);
        } else if (name.equals("write")) {
            checkArgument(type.equals(methodType(NBTBase.class, callingClass)));
            return NBTSerializers.makeWriterCallSite(typeSpec, getter, setter);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private ToNbtBootstrap() { }

}
