package de.take_weiland.mods.commons.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor Interface as being a setter for a field.</p>
 * <p>The method must take exactly two parameters. The first one, non-primitive, defines the class that holds the field.
 * The second parameter must match the field type.
 * The return type of the method must be void.</p>
 * <p>When calling this method on a generated accessor instance, the first parameter is ignored for static fields (pass null),
 * otherwise it serves as the instance to set the field on.</p>
 * <p>Example:<pre><code>
 * &#0064;Setter(field = "foo")
 * void setFoo(T obj, int foo);
 *
 * &#0064;Setter(field = "field_12345_a", srg = true)
 * void setBar(T obj, int bar);
 * </code></pre></p>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Setter {

	/**
	 * <p>The field to set. If it is an obfuscated field, provide the SRG name (e.g. field_12345_a)
	 * and set {@link #srg()} to true.</p>
	 *
	 * @return the field to set
	 */
	String field();

	/**
	 * <p>Set this to true if the field name provided by {@link #field()} is an SRG name.</p>
	 *
	 * @return true if it is a SRG field
	 */
	boolean srg() default false;

}
