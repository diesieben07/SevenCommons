package de.take_weiland.mods.commons.reflect;

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
	 * The method to invoke. If it is an obfuscated method, provide the SRG name (e.g. func_123456_a)
	 * and set {@link #srg()} to true
	 * @return the method to invoke
	 */
	String method();

	/**
	 * Set this to true if the field name provided by {@link #field()} is an SRG name
	 * @return true if it's a SRG field
	 */
	boolean srg() default false;
	
}
