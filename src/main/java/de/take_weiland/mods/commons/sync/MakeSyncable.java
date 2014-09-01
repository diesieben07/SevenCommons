package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.AnnotationNull;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Implement this on a class and it will be made {@link de.take_weiland.mods.commons.sync.Syncable}.</p>
 * <p>Use the {@code @Watch} annotation on properties to define which properties of this class should be watched
 * for changes.</p>
 *
 * @see de.take_weiland.mods.commons.sync.Syncable
 * @author diesieben07
 */
public interface MakeSyncable extends SyncableProxy {

	/**
	 * <p>Mark a property to be watched for changes in a class that implements {@code MakeSyncable}.</p>
	 * <p>Works similar to {@link de.take_weiland.mods.commons.sync.Sync @Sync}, see there for details.</p>
	 */
	@Retention(RUNTIME)
	@Target({ FIELD, METHOD })
	public @interface Watch {

		/**
		 * <p>Set this to false if this property can never be null.</p>
		 * @return if this property can be null
		 */
		boolean nullable() default true;

		/**
		 * <p>Define the actual type to sync this property as.</p>
		 * @return the actual type
		 */
		Class<?> syncAs() default AnnotationNull.class;

	}

}
