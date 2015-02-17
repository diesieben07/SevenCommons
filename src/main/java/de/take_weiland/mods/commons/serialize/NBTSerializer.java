package de.take_weiland.mods.commons.serialize;

import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.internal.AnnotationNull;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>A serializer that can serialize values of type {@code T} to NBT.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public interface NBTSerializer<T> {

	/**
	 * <p>Serialize the value of the given property to NBT.</p>
	 * @param property the property
	 * @param instance the instance
	 * @return NBT data
	 */
	@Nonnull
	<OBJ> NBTBase serialize(Property<T, OBJ> property, OBJ instance);

	/**
	 * <p>Deserialize the given property from NBT.</p>
	 * @param nbt the NBT data
	 * @param property the property
	 * @param instance the instance
	 */
	<OBJ> void deserialize(NBTBase nbt, Property<T, OBJ> property, OBJ instance);

	/**
	 * <p>A Provider for NBTSerializers.</p>
	 *
	 * <p>When applied to a method, this method must be static and accept a single parameter of type
	 * {@link TypeSpecification}. It's return type must be any non-primitive type.</p>
	 *
	 * <p>When applied to a field, this field must be static and final. It's type must be assignable to {@code NBTSerializer}
	 * resp. {@code NBTSerializer.Contents}. The {@linkplain #method() SerializationMethod filter} must be set when applied
	 * to fields.</p>
	 *
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
		 * type or subtypes. In other words: if this attribute reports {@code T.class}, this provider will only be queried
		 * for TypeSpecifications of type {@code TypeSpecification&lt;? extends T&gt;}.</p>
		 *
		 * <p>If this method is left at default, the class declaring the provider will be used.</p>
		 *
		 * @return the base type
		 */
		Class<?> forType() default AnnotationNull.class;

		/**
		 * <p>A filter for the SerializationMethod that this provider supports. If {@code VALUE} or {@code CONTENTS}
		 * are specified, this provider will only be queried for types that demand that SerializationMethod.</p>
		 *
		 * <p>A filter is mandatory for fields.</p>
		 *
		 * @return a filter
		 */
		SerializationMethod.Method method() default SerializationMethod.Method.DEFAULT;

	}

}
