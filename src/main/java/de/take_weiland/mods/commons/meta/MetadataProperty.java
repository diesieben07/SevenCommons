package de.take_weiland.mods.commons.meta;

/**
 * @author diesieben07
 */
public interface MetadataProperty<T> {

	T value(int metadata);

	int toMeta(T value, int previousMeta);
}
