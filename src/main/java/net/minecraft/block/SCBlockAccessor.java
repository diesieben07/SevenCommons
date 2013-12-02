package net.minecraft.block;

public final class SCBlockAccessor {

	private SCBlockAccessor() { }
	
	public static String getIconName(Block block) {
		return block.getTextureName();
	}

}
