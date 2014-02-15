package de.take_weiland.mods.commons.traits;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mark a static factory method in your {@link de.take_weiland.mods.commons.traits.HasTraits @HasTraits} class with this to act as bridge methods to a constructor.<br />
 * The parameters of the method should match those of the constructor and will be passed through directly<br />
 * This is the preferred way to create instances of your abstract @HasTraits class
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Factory {

}
