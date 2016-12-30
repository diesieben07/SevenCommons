package de.take_weiland.mods.commons.serialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Specify if property should be treated as a value or a "container" for some data.</p>
 *
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface RequestSerializationMethod {

    SerializationMethod value();

}
