package de.take_weiland.mods.commons.fastreflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * <p>Define a method in an Accessor interface as being a setter for a Field.</p>
 * <p>The method must take exactly two parameters. The first one, non-primitive, defines the class that holds the field.
 * The second parameter must match the field type.
 * The return type of this method must be void.</p>
 * <p>When calling this method on a generated accessor instance, the first parameter is ignored for static fields (pass null), otherwise it serves as the instance to set the field on.</p>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Setter {

	/**
	 * define which field to set, the names are tried in order (mainly used for fields that are obfuscated outside the development environment)
	 */
	String[] field();
	
}
