package de.take_weiland.mods.commons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public interface ContainerGui<T extends Container> {

	ResourceLocation getTexture();
	
	Minecraft getMinecraft();
	
}
