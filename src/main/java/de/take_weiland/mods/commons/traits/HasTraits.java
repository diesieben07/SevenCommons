package de.take_weiland.mods.commons.traits;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark any class that uses a trait with this<br />
 * The class should be declared abstract to avoid compile errors. To create instances of your class add a static factory method and mark it with {@link de.take_weiland.mods.commons.traits.Factory @Factory}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HasTraits {

}
