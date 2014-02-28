package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

//@Mod(modid = "testmod_sc_", name = "testmod_sc_", version = "0.1")
//@NetworkMod()
public class testmod_sc_ {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		ModMetadata meta = event.getModMetadata();
		meta.updateUrl = "http://www.take-weiland.de/test.txt";
	}

}
