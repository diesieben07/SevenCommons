package de.take_weiland.mods.commons.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor Interface as being a getter for a field.</p>
 * <p>The method must take exactly one non-primitive parameter, which defines the class that holds the field.
 * The return type of this method must match the field type.</p>
 * <p>When calling this method on a generated accessor instance, the parameter is ignored for static fields (pass null), otherwise it serves as the instance to get the field from.</p>
 * <p>Example:<pre><code>
 * &#0064;Getter(field = "foo")
 * int getFoo(T obj);
 *
 * &#0064;Getter(field = "field_12345_a", srg = true)
 * int getBar(T obj);
 * </code></pre></p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Getter {

	/**
	 * <p>The field to get. If it is an obfuscated field, provide the SRG name (e.g. {@code field_12345_a})
	 * and set {@link #srg()} to true.</p>
	 *
	 * @return the field to get
	 */
	String field();

	/**
	 * <p>Set this to true if the field name provided by {@link #field()} is an SRG name.</p>
	 *
	 * @return true if it is a SRG field
	 */
	boolean srg() default false;

}
