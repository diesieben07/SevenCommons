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
 * @author diesieben07
 */
public interface NBTSerializer<T> {

	@Nonnull
	NBTBase serialize(@Nullable T instance);

	@Nullable
	T deserialize(@Nullable NBTBase nbt);

	interface Contents<T> {

		@Nonnull
		NBTBase serialize(@Nonnull T instance);

		void deserialize(@Nullable NBTBase nbt, @Nonnull T instance);

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.FIELD})
	@interface Provider {

		Class<?> forType() default AnnotationNull.class;

	}

}
