package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.meta.MetadataProperty;

/**
 * @author diesieben07
 */
public interface SCMetaInternalProxy {

	<E> E[] backingValues(MetadataProperty<E> property);

}
