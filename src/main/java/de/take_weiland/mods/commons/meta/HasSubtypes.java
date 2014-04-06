package de.take_weiland.mods.commons.meta;

/**
 * @author diesieben07
 */
public interface HasSubtypes<T extends Enum<T> & Subtype> {

	MetadataProperty<T> subtypeProperty();

}
