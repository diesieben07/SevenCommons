package de.take_weiland.mods.commons.serialize;

/**
 * @author diesieben07
 */
public enum SerializationMethod {

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
