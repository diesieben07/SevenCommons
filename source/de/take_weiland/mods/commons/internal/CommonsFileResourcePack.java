package de.take_weiland.mods.commons.internal;

import net.minecraft.client.resources.FileResourcePack;
import cpw.mods.fml.common.ModContainer;

public class CommonsFileResourcePack extends FileResourcePack {

	public CommonsFileResourcePack(ModContainer mc) {
		super(SevenCommons.source);
	}
	
	@Override
	public String func_130077_b() {
		return "SevenCommons";
	}

}
