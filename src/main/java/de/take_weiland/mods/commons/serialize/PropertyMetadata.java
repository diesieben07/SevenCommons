package de.take_weiland.mods.commons.serialize;

import com.google.common.reflect.TypeToken;

import java.lang.annotation.Annotation;

/**
 * <p>Information about a Property.</p>
 *
 * @author diesieben07
 */
public interface PropertyMetadata<T> {

	/**
	 * <p>Return a TypeToken representing the type of the property.</p>
	 * @return a TypeToken
	 */
	TypeToken<T> getType();

	/**
	 * <p>Return the raw type of the property. The raw type of {@code List&lt;String&gt;} is {@code List}.</p>
	 * @return the raw type
	 */
	Class<? super T> getRawType();

	/**
	 * <p>Get the desired SerializationMethod for the property.</p>
	 * @return the SerializationMethod
	 */
	SerializationMethod getDesiredMethod();

	/**
	 * <p>True, if the given annotation is present on the property.</p>
	 * @param annotation the annotation
	 * @return if the annotation is present
	 */
	boolean hasAnnotation(Class<? extends Annotation> annotation);

	/**
	 * <p>Get the annotation for the specified annotation type if is present, otherwise null.</p>
	 * @param annotationClass the annotation type
	 * @return the annotation
	 */
	<A extends Annotation> A getAnnotation(Class<A> annotationClass);

}
