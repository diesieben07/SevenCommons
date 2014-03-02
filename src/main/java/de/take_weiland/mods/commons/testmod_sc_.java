package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "testmod_sc_", name = "testmod_sc_", version = "0.1")
//@NetworkMod()
public class testmod_sc_ {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		FMLInterModComms.sendMessage("sevencommons", "setUpdateUrl", "http://www.take-weiland.de/test.txt");

		TstLit t = new TstLit();
		Listenables.register(t, new Listenable.Listener<TstLit>() {
			@Override
			public void onChange(TstLit o) {
			}
		});
		Listenables.onChange(t);

	}

	private static class TstLit implements Listenable<TstLit> {

		@Override
		public void onChange() {

		}
	}

}
