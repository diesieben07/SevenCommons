package de.take_weiland.mods.commons.fastreflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor interface as being a getter for a Field.</p>
 * <p>The method must take exactly one non-primitive parameter, which defines the class that holds the field.
 * The return type of this method must match the field type.</p>
 * <p>When calling this method on a generated accessor instance, the parameter is ignored for static fields (pass null), otherwise it serves as the instance to get the field from.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Getter {

	/**
	 * define which field to get, the names are tried in order (mainly used for fields that are obfuscated outside the development environment)
	 */
	String[] field();
	
}
