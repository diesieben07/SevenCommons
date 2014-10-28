package de.take_weiland.mods.commons.internal;

import net.minecraft.inventory.IInventory;

import java.util.List;

/**
 * @author diesieben07
 */
public interface ContainerProxy {

	String CLASS_NAME = "de/take_weiland/mods/commons/internal/ContainerProxy";
	String GET_INVENTORIES = "_sc$getInventories";

	List<IInventory> _sc$getInventories();

}
