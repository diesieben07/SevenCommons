package de.take_weiland.mods.commons;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Used to define an alternate corresponding setter for a getter.</p>
 * <p>This method is not needed if your Setter follows JavaBeans naming convention ({@code Foo getFoo()} and {@code void setFoo(Foo)}.</p>
 * <p></p>
 *
 * @author diesieben07
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ExplicitSetter {

    String value();

    @Retention(CLASS)
    @Target(METHOD)
    @interface ScalaSetter {
    }

}
