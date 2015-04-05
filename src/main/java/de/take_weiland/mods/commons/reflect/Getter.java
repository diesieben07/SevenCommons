package de.take_weiland.mods.commons.reflect;

import de.take_weiland.mods.commons.internal.AnnotationNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor Interface as being a getter for a field.</p>
 * <p>For static targets the method must take no parameters. For non-static targets the method must take exactly
 * one parameter of a reference type specifying the instance to get the field from.</p>
 * <p>For static targets {@link #target()} specifies the target class, otherwise the type of the parameter is used.</p>
 * <p>{@link OverrideTarget &#0064;OverrideTarget} on the method may be used in any case to override the target class.</p>
 * <p>The return type of the method must be {@linkplain Class#isAssignableFrom(Class) assignable from} or the same
 * as the target field type.</p>
 * <p>Examples (declaration as in the accessor interface and symbolic implementation):<pre><code>
 * &#0064;Getter(field = "foo", target = FooBar.class)
 * int getFoo();
 *
 * &#0064;Getter(field = "field_12345_a", srg = true)
 * int getBar(FooBar obj);
 *
 * int getFoo() {
 *     return FooBar.foo;
 * }
 *
 * int getBar(FooBar obj) {
 *     return obj.field_12345_a;
 * }
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

	Class<?> target() default AnnotationNull.class;

}
