package de.take_weiland.mods.commons.reflect;

import de.take_weiland.mods.commons.internal.AnnotationNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor Interface as being a wrapper for another method.</p>
 * <p>For static targets the method must take exactly the number of parameters of the target method. For non-static targets
 * the method must take exactly the number of parameters of the target method, preceded by a reference parameter specifying
 * the instance to invoke the method on.</p>
 * <p>For static targets {@link #target()} specifies the target class, otherwise the type of the first parameter is used.</p>
 * <p>{@link OverrideTarget &#0064;OverrideTarget} on the method may be used in any case to override the target class.</p>
 * <p>The return type of the method must be {@linkplain Class#isAssignableFrom(Class) assignable from} or the same as
 * the return type of the target method. The same must hold for the parameter types (each parameter type of the target method
 * must be assignable from or the same as the corresponding parameter type of the accessor method).</p>
 * <p>If a parameter type cannot be declared accurately in the accessor class due to access restrictions
 * (e.g. a package-local class) {@code &#0064;OverrideTarget} <i>must</i> be used on that parameter to specify
 * the exact parameter type.</p>
 * <p>Examples (declaration as in the accessor interface and symbolic implementation):<pre><code>
 * &#0064;Invoke(method = "doSomething", target = FooBar.class)
 * void doSomething(int foo, String bar);
 * <p>
 * &#0064;Invoke(method = "func_12345_a", srg = true)
 * String doSomethingElse(FooBar self, String bar);
 * <p>
 * void doSomething(int foo, String bar) {
 *     FooBar.doSomething(foo, bar);
 * }
 * <p>
 * String doSomethingElse(FooBar self, String bar) {
 *     return self.func_12345_a(bar);
 * }
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

    /**
     * <p>The class containing the target method. Only used if the target method is static.</p>
     *
     * @return the target class
     */
    Class<?> target() default AnnotationNull.class;

}
