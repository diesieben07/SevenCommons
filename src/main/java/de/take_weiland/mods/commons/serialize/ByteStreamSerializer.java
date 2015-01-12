package de.take_weiland.mods.commons.serialize;

import de.take_weiland.mods.commons.internal.AnnotationNull;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>A serializer that can serialize values of type {@code T} to a byte stream.</p>
 *
 * @author diesieben07
 */
public interface ByteStreamSerializer<T> {

	/**
	 * <p>Serialize the given instance to the OutputStream.</p>
	 * @param instance the instance
	 * @param out the OutputStream
	 */
	void write(T instance, MCDataOutput out);

	/**
	 * <p>Read an instance of type T from the InputStream.</p>
	 * @param in the IntputStream
	 * @return the instance
	 */
	T read(MCDataInput in);

	/**
	 * <p>A serializer that can serialize the contents of objects of type {@code T} to a byte stream.
	 * As opposed to {@link de.take_weiland.mods.commons.serialize.ByteStreamSerializer}
	 * such a serializer never touches the instance being passed in but directly modifies it's contents.</p>
	 * <p>This is used for e.g. FluidTanks.</p>
	 *
	 */
	interface Contents<T> {

		/**
		 * <p>Serialize the contents of the given instance to the OutputStream.</p>
		 * @param instance the instance
		 * @param out the OutputStream
		 */
		void write(T instance, MCDataOutput out);

		/**
		 * <p>Deserialize the the contents of the given instance from the InputStream.</p>
		 * @param instance the instance
		 * @param in the InputStream
		 */
		void read(T instance, MCDataInput in);

	}

	/**
	 * <p>A Provider for ByteStreamSerializers.</p>
	 * <p>When applied to a method, this method must be static and accept a single parameter of type
	 * {@link PropertyMetadata}. It's return type must be any non-primitive type.</p>
	 *
	 * <p>When applied to a field, this field must be static and final. It's type must be assignable to {@code ByteStreamSerializer}
	 * resp. {@code ByteStreamSerializer.Contents}. The {@linkplain #method() SerializationMethod filter} must be set when applied
	 * to fields.</p>
	 *
	 * <p>The TypeSpecification dictates what this provider must provide. The TypeSpecification will only ever report
	 * the concrete SerializationMethods {@code VALUE} or {@code CONTENTS}. The provider must then return an instance of
	 * {@code ByteStreamSerializer} or {@code ByteStreamSerializer.Contents} respectively. If this provider cannot provide a
	 * serializer for the given TypeSpecification, {@code null} must be returned.</p>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.FIELD})
	@interface Provider {

		/**
		 * <p>The base type that this provider can provide serializers for. This provider will only be queried for that
		 * type or subtypes. In other words: if this attribute reports {@code T.class}, this provider will only be queried
		 * for TypeSpecifications of type {@code TypeSpecification&lt;? extends T&gt;}.</p>
		 *
		 * <p>If this method is left at default, the class declaring the provider will be used.</p>
		 *
		 * @return the base type
		 */
		// if name is changed, need to update SerializerRegistry as well!
		Class<?> forType() default AnnotationNull.class;

		/**
		 * <p>A filter for the SerializationMethod that this provider supports. If {@code VALUE} or {@code CONTENTS}
		 * are specified, this provider will only be queried for types that demand that SerializationMethod.</p>
		 *
		 * <p>A filter is mandatory for fields.</p>
		 *
		 * @return a filter
		 */
		SerializationMethod method() default SerializationMethod.DEFAULT;

	}

}
