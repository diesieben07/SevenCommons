package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;

/**
 * @author diesieben07
 */
public interface NBTWriterFactory {

    MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter);

}
