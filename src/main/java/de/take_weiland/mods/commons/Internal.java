package de.take_weiland.mods.commons;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Elements marked with this annotation are internal to an implementation and should not be used by client code, even if visible
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ TYPE, METHOD, CONSTRUCTOR, FIELD } )
public @interface Internal {

}
