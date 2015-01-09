package de.take_weiland.mods.commons.serialize;

import de.take_weiland.mods.commons.internal.AnnotationNull;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author diesieben07
 */
public interface ByteStreamSerializer<T> {

	void write(T obj, MCDataOutput out);

	T read(MCDataInput in);

	interface Contents<T> {

		void write(T obj, MCDataOutput out);

		void read(T obj, MCDataInput in);

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.FIELD})
	@interface Provider {

		// if name is changed, need to update SerializerRegistry as well!
		Class<?> forType() default AnnotationNull.class;

	}

}
