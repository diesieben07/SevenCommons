package de.take_weiland.mods.commons;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Objects marked with this Annotation should be treated carefully as they perform potentially unsafe operations if used incorrectly
 *
 */
@Target({ TYPE, METHOD, CONSTRUCTOR } )
@Retention(SOURCE)
public @interface Unsafe {

}
