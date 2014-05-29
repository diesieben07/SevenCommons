package de.take_weiland.mods.commons.internal;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author diesieben07
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface SCTransformed {
}
