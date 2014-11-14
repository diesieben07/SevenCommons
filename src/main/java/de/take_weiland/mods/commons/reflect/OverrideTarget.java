package de.take_weiland.mods.commons.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Override the target class name for an Accessor Interface method. This is useful when the actual class containing the
 * target is not visible to the Accessor Interface.</p>
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OverrideTarget {

	/**
	 * <p>The name of the class to use as the actual target for teh accessor method. The class must be a assignable to the
	 * target class used in the signature of the target method.</p>
	 * @return a class name
	 */
	String value();

}
