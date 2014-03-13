package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.metadata.Metadata;

/**
 * @author diesieben07
 */
public interface ItemStackProxy {

	public static final String GETTER = "_sc$getMetadata";

	<T extends Metadata> T _sc$getMetadata();

}
