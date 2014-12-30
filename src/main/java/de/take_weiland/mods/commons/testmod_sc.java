package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

import javax.annotation.Nonnull;
import java.io.IOException;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "after:SevenCommons")
@NetworkMod()
public class testmod_sc {

	private static Enum enumSet;

	public static void main(@Nonnull String[] bar) throws NoSuchFieldException {
	}

	@Mod.Instance
	public static testmod_sc instance;

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) throws NoSuchMethodException, NoSuchFieldException, IOException {
		System.out.println(new TestTE());
		System.exit(0);
	}

}
