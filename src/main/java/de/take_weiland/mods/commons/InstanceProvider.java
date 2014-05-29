package de.take_weiland.mods.commons;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * @author diesieben07
 */
@Retention(CLASS)
@Target(METHOD)
public @interface InstanceProvider {

}
