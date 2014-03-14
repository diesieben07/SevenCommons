package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.metadata.Metadata;
import net.minecraft.item.ItemStack;

/**
 * @author diesieben07
 */
public interface MetadataItemProxy<T extends Metadata> {

	public static final String GETTER = "_sc$getMetadata";

	T _sc$getMetadata(ItemStack stack);

}
