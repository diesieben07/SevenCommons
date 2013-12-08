package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

//@Mod(modid = "testmod", name = "testmod", version = "0.1")
public class testmod_renamed {

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModMetadata meta = event.getModMetadata();
		meta.updateUrl = "http://www.take-weiland.de/test.txt";
	}

}
