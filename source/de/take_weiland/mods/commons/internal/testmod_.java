package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

//@Mod(name ="testmod", modid = "testmod", version = "0.8")
public class testmod_ {

	
	@EventHandler
	public void onPreINit(FMLPreInitializationEvent event) {
		event.getModMetadata().updateUrl = "file:///C:/Users/Take/Desktop/test.txt";
	}
	
}
