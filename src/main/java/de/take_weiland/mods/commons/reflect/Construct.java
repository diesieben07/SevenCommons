package de.take_weiland.mods.commons.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor Interface as being a wrapper for a constructor.</p>
 * <p>The method must take the exactly the number of parameters as the target constructor.</p>
 * <p>The target class is specified by the return type of the method, which must be a reference type. The target
 * class may be overridden using {@link OverrideTarget &#0064;OverrideTarget} on the method.</p>
 * <p>Each parameter of the target constructor must be {@linkplain Class#isAssignableFrom(Class) assignable from} or
 * the same as the corresponding parameter in the accessor method.</p>
 * <p>If a parameter type cannot be declared accurately in the accessor class due to access restrictions
 * (e.g. a package-local class) {@code &#0064;OverrideTarget} <i>must</i> be used on that parameter to specify
 * the exact parameter type.</p>
 * <p>Example:<pre><code>
 * &#0064;Construct
 * FooBar newFoobar(int a, String b);
 *
 * FooBar newFoobar(int a, String b) {
 *     return new FooBar(a, b);
 * }
 * </code></pre></p>
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Construct {

}
