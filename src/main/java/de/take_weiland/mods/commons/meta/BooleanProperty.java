package de.take_weiland.mods.commons.meta;

/**
 * @author diesieben07
 */
public interface BooleanProperty extends MetadataProperty<Boolean> {

	boolean booleanValue(int metadata);

	int toMeta(boolean value, int previousMeta);

}
