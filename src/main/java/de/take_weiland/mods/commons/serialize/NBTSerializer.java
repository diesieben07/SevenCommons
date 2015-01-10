package de.take_weiland.mods.commons.serialize;

import de.take_weiland.mods.commons.internal.AnnotationNull;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>A serializer that can serialize values of type {@code T} to NBT.</p>
 *
 * @author diesieben07
 */
public interface NBTSerializer<T> {

	/**
	 * <p>Serialize the given instance to NBT.</p>
	 * @param instance the instance
	 * @return NBT data
	 */
	@Nonnull
	NBTBase serialize(T instance);

	/**
	 * <p>Deserialize the given NBT data into an instance.</p>
	 * @param nbt the NBT data
	 * @return the deserialized instance
	 */
	T deserialize(@Nullable NBTBase nbt);

	/**
	 * <p>A serializer that can serialize the contents of objects of type {@code T} to NBT.
	 * As opposed to {@link de.take_weiland.mods.commons.serialize.NBTSerializer}
	 * such a serializer never touches the instance being passed in but directly modifies it's contents.</p>
	 * <p>This is used for e.g. FluidTanks.</p>
	 *
	 */
	interface Contents<T> {

		/**
		 * <p>Serialize the contents of the given instance to NBT.</p>
		 * @param instance the instance
		 * @return NBT data
		 */
		@Nonnull
		NBTBase serialize(@Nonnull T instance);

		/**
		 * <p>Deserialize the given NBT data into the contents of the given instance.</p>
		 * @param instance the instance
		 * @param nbt the NBT data
		 */
		void deserialize(@Nonnull T instance, @Nullable NBTBase nbt);

	}

	/**
	 * <p>A Provider for NBTSerializers.</p>
	 * <p>When applied to a method, this method must be static and accept a single parameter of type
	 * {@link de.take_weiland.mods.commons.serialize.TypeSpecification}. It's return type must be any non-primitive type.</p>
	 * <p>When applied to a field, this field must be static and final. It's type must be assignable to {@code NBTSerializer}
	 * resp. {@code NBTSerializer.Contents}. The {@linkplain #method() SerializationMethod filter} must be set when applied
	 * to fields.</p>
	 * <p>The TypeSpecification dictates what this provider must provide. The TypeSpecification will only ever report
	 * the concrete SerializationMethods {@code VALUE} or {@code CONTENTS}. The provider must then return an instance of
	 * {@code NBTSerializer} or {@code NBTSerializer.Contents} respectively. If this provider cannot provide a serializer
	 * for the given TypeSpecification, {@code null} must be returned.</p>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.FIELD})
	@interface Provider {

		/**
		 * <p>The base type that this provider can provide serializers for. This provider will only be queried for that
		 * type or subtypes.</p>
		 * <p>If this method is left at default, the class declaring the provider will be used.</p>
		 * @return the base type
		 */
		Class<?> forType() default AnnotationNull.class;

		/**
		 * <p>A filter for the SerializationMethod that this provider supports. If {@code VALUE} or {@code CONTENTS}
		 * are specified, this provider will only be queried for types that demand that SerializationMethod.</p>
		 * <p>A filter is mandatory for fields.</p>
		 * @return a filter
		 */
		SerializationMethod method() default SerializationMethod.DEFAULT;

	}

}
