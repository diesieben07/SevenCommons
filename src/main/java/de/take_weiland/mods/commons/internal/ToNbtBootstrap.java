package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.nbt.NBTSerializers;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import net.minecraft.nbt.NBTBase;

import java.lang.invoke.*;

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

        MethodHandle mh;

        if (name.equals("read")) {
            checkArgument(type.equals(methodType(void.class, callingClass, NBTBase.class)));
            mh = NBTSerializers.makeReader(typeSpec, getter, setter);
        } else if (name.equals("write")) {
            checkArgument(type.equals(methodType(NBTBase.class, callingClass)));
            mh = NBTSerializers.makeWriter(typeSpec, getter, setter);
        } else {
            throw new IllegalArgumentException();
        }

        if (mh == null) {
            mh = MethodHandles.throwException(type.returnType(), RuntimeException.class);
            mh = MethodHandles.filterArguments(mh, 0, newRTEx()).bindTo("Could not find serializer for " + typeSpec);
            mh = MethodHandles.dropArguments(mh, 0, type.parameterArray());
        }
        return new ConstantCallSite(mh);
    }

    private static MethodHandle NEW_RT_EX;

    private static MethodHandle newRTEx() {
        if (NEW_RT_EX == null) {
            try {
                NEW_RT_EX = MethodHandles.lookup().findConstructor(RuntimeException.class, methodType(void.class, String.class));
            } catch (ReflectiveOperationException e) {
                throw Throwables.propagate(e);
            }
        }
        return NEW_RT_EX;
    }

    private ToNbtBootstrap() { }

}
