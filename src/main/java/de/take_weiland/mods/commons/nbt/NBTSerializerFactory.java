package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;

/**
 * <p>A factory for generating NBT serializers for {@link de.take_weiland.mods.commons.nbt.ToNbt @ToNbt}.</p>
 *
* @author diesieben07
*/
public interface NBTSerializerFactory {

    /**
     * <p>Generate a MethodHandle that reads a property from NBT.</p>
     * <p>Given the two types {@code C} and {@code T} the getter will have type {@code (C)->T} and the setter will have type
     * {@code (C, T)->void}.</p>
     * <p>This method must then produce a MethodHandle of the exact type {@code (C, NBTBase)->void} which must perform
     * all steps necessary to deserialize the property, including any needed invocation of the getter or setter.</p>
     * <p>If the given TypeSpecification is not supported by this factory, {@code null} must be returned instead.</p>
     * @param typeSpec the TypeSpecification for the property to be serialized
     * @param getter the getter for the property
     * @param setter the setter for the property
     * @return a MethodHandle or null
     */
    MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter);

    /**
     * <p>Generate a MethodHandle that writes a property to NBT.</p>
     * <p>Given the two types {@code C} and {@code T} the getter will have type {@code (C)->T} and the setter will have type
     * {@code (C, T)->void}.</p>
     * <p>This method must then produce a MethodHandle of the exact type {@code (C)->NBTBase} which must perform
     * all steps necessary to serialize the property, including any needed invocation of the getter or setter.</p>
     * <p>If the given TypeSpecification is not supported by this factory, {@code null} must be returned instead.</p>
     * @param typeSpec the TypeSpecification for the property to be serialized
     * @param getter the getter for the property
     * @param setter the setter for the property
     * @return a MethodHandle or null
     */
    MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter);

}
