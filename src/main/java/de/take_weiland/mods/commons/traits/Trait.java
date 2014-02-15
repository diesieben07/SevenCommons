package de.take_weiland.mods.commons.traits;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an Interface as a Trait, the implementation class should implement this interface and have a no-arg constructor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Trait {

	Class<?> impl();

}
