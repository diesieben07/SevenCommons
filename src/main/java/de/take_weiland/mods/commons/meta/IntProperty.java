package de.take_weiland.mods.commons.meta;

/**
 * @author diesieben07
 */
public interface IntProperty extends MetadataProperty<Integer> {

	int intValue(int metadata);

	int toMeta(int value, int previousMeta);

}
