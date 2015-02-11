package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;

/**
 * @author diesieben07
 */
public abstract class NBTSerializerWrapper {

    public abstract MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter);

    public abstract MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter);


}
