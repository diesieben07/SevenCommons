package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;

/**
 * @author diesieben07
 */
public interface NBTReaderFactory {

    MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter);

}
