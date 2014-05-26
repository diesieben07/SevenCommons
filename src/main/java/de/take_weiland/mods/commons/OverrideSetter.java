package de.take_weiland.mods.commons;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Used to define an alternate corresponding setter for a getter.<br/>
 * This method is not needed if your Setter follows the following naming conventions:<br/>
 * <ul>
 *     <li>{@code getFoo} corresponds to {@code setFoo}</li>
 * </ul>
 *
 * @author diesieben07
 */
@Retention(CLASS)
@Target(METHOD)
public @interface OverrideSetter {

	String value();

}
