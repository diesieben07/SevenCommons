package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.metadata.Metadata;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
public interface MetadataBlockProxy<T extends Metadata> extends MetadataItemProxy<T> {

	T _sc$getMetadata(Object holder, World world, int x, int y, int z);

}
