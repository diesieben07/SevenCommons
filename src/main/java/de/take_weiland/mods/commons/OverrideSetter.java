package de.take_weiland.mods.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author diesieben07
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.LOCAL_VARIABLE })
public @interface OverrideSetter {

	String value();

}
