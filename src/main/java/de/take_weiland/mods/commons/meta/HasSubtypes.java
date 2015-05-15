package de.take_weiland.mods.commons.meta;

/**
 * <p>When implemented on a Block or Item specifies the subtypes of that Block or Item.</p>
 *
 * @author diesieben07
 */
public interface HasSubtypes<T extends Subtype> {

    /**
     * <p>Return the MetadataProperty that represents the subtypes of ths Block or Item.</p>
     *
     * @return a MetadataProperty
     */
    MetadataProperty<T> subtypeProperty();

}
