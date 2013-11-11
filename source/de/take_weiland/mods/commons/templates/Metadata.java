package de.take_weiland.mods.commons.templates;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface Metadata {

	// named ordinal for easier Enums
	int ordinal();
	
	String unlocalizedName();
	
	public static interface ItemMeta extends Metadata {
		
		Item getItem();
		
	}
	
	public static interface BlockMeta extends Metadata {
		
		Block getBlock();
		
	}
	
}
