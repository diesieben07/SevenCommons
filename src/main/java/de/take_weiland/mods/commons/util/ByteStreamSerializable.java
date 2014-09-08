package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.MCDataOutputStream;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Implementations of this interface can be serialized to a bytestream, usually for networking purposes.</p>
 * <p>In addition to the the {@code writeTo} method, implementations are required to have a {@code public static}
 * method, with a return type of the implementing class or a subclass thereof. It must take a single parameter, an
 * {@link de.take_weiland.mods.commons.net.MCDataInputStream} or a superclass thereof. That method is used to deserialize
 * the objects from stream and needs to be marked with {@link de.take_weiland.mods.commons.util.ByteStreamSerializable.Deserializer}.</p>
 * <p>Example:
 * <code><pre>
 *     &#0064;Deserializer
 *     public static T deserialize(MCDataInputStream in) {
 *         return new T(in.readInt());
 *     }
 * </pre></code></p>
 * <p>A class implementing {@code ByteStreamSerializable} can be turned into a {@code ByteStreamSerializer} via
 * {@link de.take_weiland.mods.commons.util.Serializers#wrap(Class, boolean)} .</p>
 *
 * @author diesieben07
 */
public interface ByteStreamSerializable {

	/**
	 * <p>Write this object to the stream.</p>
	 * @param out the stream
	 */
	void writeTo(MCDataOutputStream out);

	/**
	 * <p>Marker for the deserialization method. See {@link de.take_weiland.mods.commons.util.ByteStreamSerializable}.</p>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Deserializer { }

}
