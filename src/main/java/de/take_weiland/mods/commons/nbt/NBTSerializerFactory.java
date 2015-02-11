package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;

/**
* @author diesieben07
*/
public interface NBTSerializerFactory {

    MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter);

    MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter);

}
