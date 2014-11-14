package de.take_weiland.mods.commons.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor Interface as being a wrapper for another method.</p>
 * <p>The method must take at least one parameter, which defines the class the target method is in. Following after that must be all parameters of the target method.
 * The return type of this method must match the return type of the target method.</p>
 * <p>When calling this method on a generated accessor instance, the first parameter is ignored for static methods (pass null), otherwise it serves as the
 * instance to invoke the method on.</p>
 * <p>Example:<pre><code>
 * &#0064;Invoke(method = "doSomething")
 * void doSomething(T self, int foo, String bar);
 *
 * &#0064;Invoke(method = "func_12345_a", srg = true)
 * void doSomethingElse(T self, String bar);
 * </code></pre></p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Invoke {

	/**
	 * <p>The method to invoke. If it is an obfuscated method, provide the SRG name (e.g. func_12345_a)
	 * and set {@link #srg()} to true.</p>
	 *
	 * @return the method to invoke
	 */
	String method();

	/**
	 * <p>Set this to true if the method name provided by {@link #method()} is an SRG name.</p>
	 *
	 * @return true if it is a SRG method
	 */
	boolean srg() default false;

}
