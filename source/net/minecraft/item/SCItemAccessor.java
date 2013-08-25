package net.minecraft.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class SCItemAccessor {

	private SCItemAccessor() { }

	@SideOnly(Side.CLIENT)
	public static String getIconName(Item item) {
		return item.func_111208_A();
	}
	
	public static void setHasSubtypes(Item item) {
		item.setHasSubtypes(true);
	}
	
}
