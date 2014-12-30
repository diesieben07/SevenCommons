package de.take_weiland.mods.commons.sync;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.AnnotatedElement;

/**
 * <p>Metadata for a Property, represented by a Field or a getter and a setter. Only annotations on the getter are
 * recognized in the 2nd case.</p>
 *
 * @author diesieben07
 */
public interface PropertyMetadata extends AnnotatedElement {

	/**
	 * <p>Get the generic type of this property.</p>
	 * @return a TypeToken
	 */
	TypeToken<?> getType();

	/**
	 * <p>Get the raw type of this property, equivalent to {@code getType().getRawType()}, but potentially more efficient.</p>
	 * @return
	 */
	Class<?> getRawType();

}
