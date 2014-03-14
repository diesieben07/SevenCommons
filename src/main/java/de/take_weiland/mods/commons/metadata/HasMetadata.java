package de.take_weiland.mods.commons.metadata;

/**
 * @author diesieben07
 */

public interface HasMetadata<T extends Metadata> {

	Class<T> metaClass();

	public static interface Extended<T extends Metadata.Extended> extends HasMetadata<T> { }

	public static interface Simple<T extends Enum<T> & Metadata.Simple> extends HasMetadata<T> { }

}
