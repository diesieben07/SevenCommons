package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.ModContainer;
import net.minecraft.client.resources.FileResourcePack;

public class CommonsFileResourcePack extends FileResourcePack {

	public CommonsFileResourcePack(ModContainer mc) {
		super(CommonsModContainer.instance.getSource());
	}
	
	@Override
	public String func_130077_b() {
		return "SevenCommons";
	}

}
