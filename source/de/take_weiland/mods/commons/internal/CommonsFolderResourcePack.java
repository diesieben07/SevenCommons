package de.take_weiland.mods.commons.internal;

import net.minecraft.client.resources.FolderResourcePack;
import cpw.mods.fml.common.ModContainer;

public class CommonsFolderResourcePack extends FolderResourcePack {

	public CommonsFolderResourcePack(ModContainer mc) {
		super(CommonsModContainer.instance.getSource());
	}
	
	@Override
	public String func_130077_b() {
		return "SevenCommons";
	}

}
