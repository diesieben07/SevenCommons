package de.take_weiland.mods.commons.metadata;

/**
 * @author diesieben07
 */
public interface HasMetadata<T extends Metadata> {

	Class<T> metaClass();

}
