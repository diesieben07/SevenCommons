package de.take_weiland.mods.commons.serialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author diesieben07
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Serializer {

}
