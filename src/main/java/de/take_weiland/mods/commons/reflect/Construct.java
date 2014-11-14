package de.take_weiland.mods.commons.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Define a method in an Accessor Interface as being a pseudo-constructor. The method must take the same number and types of
 * arguments as the constructor to invoke. The return type specifies the class that is constructed.</p>
 * <p>Example:<pre><code>
 * &#0064;Construct
 * String newString(char[] data, boolean share);
 * </code></pre></p>
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Construct {

}
