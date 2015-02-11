package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.internal.AnnotationNull;

/**
 * @author diesieben07
 */
public @interface SerializerTypes {

    Class<?> value() default AnnotationNull.class;

    boolean allSubtypes() default false;

}
