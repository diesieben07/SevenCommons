package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableSet;
import net.minecraft.inventory.IInventory;

/**
 * @author diesieben07
 */
public interface ContainerProxy {

	String CLASS_NAME = "de/take_weiland/mods/commons/internal/ContainerProxy";
	String GET_INVENTORIES = "_sc$getInventories";

	ImmutableSet<IInventory> _sc$getInventories();

}
