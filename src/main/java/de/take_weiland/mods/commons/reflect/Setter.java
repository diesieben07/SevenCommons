package de.take_weiland.mods.commons.reflect;

import de.take_weiland.mods.commons.internal.AnnotationNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor Interface as being a setter for a field.</p>
 * <p>For static targets the method must take one parameter. For non-static targets that parameter must be preceded
 * by a reference type specifying the instance to set the field on.</p>
 * <p>For static targets {@link #target()} specifies the target class, otherwise the type of the first parameter is used.</p>
 * <p>{@link OverrideTarget &#0064;OverrideTarget} on the method may be used in any case to override the target class.</p>
 * <p>The return type of the method must be {@code void}. The target field type must be {@linkplain Class#isAssignableFrom(Class) assignable from}
 * or the same as the last parameter type.</p>
 * <p>Examples (declaration as in the accessor interface and symbolic implementation):<pre><code>
 * &#0064;Setter(field = "foo", target = FooBar.class)
 * void setFoo(int foo);
 *
 * &#0064;Setter(field = "field_12345_a", srg = true)
 * void setBar(FooBar obj, int bar);
 *
 * void setFoo(int foo) {
 *     FooBar.foo = foo;
 * }
 *
 * void setBar(FooBar obj, int bar) {
 *     obj.field_12345_a = bar;
 * }
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

	Class<?> target() default AnnotationNull.class;
}
