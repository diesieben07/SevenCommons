package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

import javax.annotation.Nonnull;
import java.io.IOException;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
@NetworkMod()
public class testmod_sc {

	private static Enum enumSet;

	public static void main(@Nonnull String[] bar) throws NoSuchFieldException {
	}

	@Mod.Instance
	public static testmod_sc instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws NoSuchMethodException, NoSuchFieldException, IOException {
		new TestTE();
		System.exit(0);
	}

}
