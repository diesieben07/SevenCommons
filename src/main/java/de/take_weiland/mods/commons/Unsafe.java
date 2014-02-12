package de.take_weiland.mods.commons;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Objects marked with this Annotation should be treated carefully as they perform potentially unsafe operations if used incorrectly
 *
 */
@Target({ TYPE, METHOD, CONSTRUCTOR, FIELD } )
@Retention(SOURCE)
public @interface Unsafe {

}
