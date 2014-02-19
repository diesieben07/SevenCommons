package de.take_weiland.mods.commons.fastreflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor interface as being a wrapper method to another method.</p>
 * <p>The method must take at least one parameter, which defines the class the target method is in. Following after that must be all parameters of the target method.
 * The return type of this method must match the return type of the target methods.</p>
 * <p>When calling this method on a generated accessor instance, the first parameter is ignored for static methods (pass null), otherwise it serves as the instance to invoke the method on.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Invoke {

	/**
	 * define which method to invoke, the names are tried in order (mainly used for methods that are obfuscated outside the development environment)
	 */
	String[] method();
	
}
