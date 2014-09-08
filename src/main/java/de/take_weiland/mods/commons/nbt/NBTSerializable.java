package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Implementations if this interface support serialization to NBT.</p>
 * <p>In addition to the {@code writeTo} method an implementing class T is also required to have a {@code public static}
 * method, with return type T or a subtype of T. It must take a single argument an {@code NBTBase} instance.
 * This method is used to read the serialized object back from NBT. It has to be marked with
 * {@link de.take_weiland.mods.commons.nbt.NBTSerializable.Deserializer}.</p>
 * <p>Example:
 * <code><pre>
 *     &#0064;Deserializer
 *     public static T deserialize(NBTBase nbt) {
 *         return new T(((NBTTagInt) nbt).data);
 *     }
 * </pre></code></p>
 *
 * @author diesieben07
 */
public interface NBTSerializable {

	/**
	 * <p>Create a serialized form of this object.</p>
	 * @return a serialized form
	 */
	NBTBase serialize();

	/**
	 * <p>Marker for the deserialization method. See {@link de.take_weiland.mods.commons.nbt.NBTSerializable}.</p>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Deserializer { }

}
