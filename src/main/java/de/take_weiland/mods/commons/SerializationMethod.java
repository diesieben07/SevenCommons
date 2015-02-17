package de.take_weiland.mods.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD })
public @interface SerializationMethod {

    SerializationMethod.Method value();

    /**
     * <p>Methods of serialization.</p>
     */
    enum Method {

        /**
         * <p>Prefer {@code VALUE}, but use {@code CONTENTS} if {@code VALUE} is not available.</p>
         */
        DEFAULT,

        /**
         * <p>Serialize the type as a value.</p>
         */
        VALUE,
        /**
         * <p>Serialize the contents of the type.</p>
         */
        CONTENTS
    }
}
