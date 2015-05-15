package de.take_weiland.mods.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Specify if property should be treated as a value or a "container" for some data.</p>
 * <p>Currently only used in conjunction with {@link de.take_weiland.mods.commons.sync.Sync @Sync} and
 * {@link de.take_weiland.mods.commons.nbt.ToNbt @ToNbt}.</p>
 *
 * @author diesieben07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SerializationMethod {

    SerializationMethod.Method value();

    enum Method {

        /**
         * <p>Let the implementation decide the most appropriate of {@code VALUE} and {@code CONTENTS}.</p>
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
