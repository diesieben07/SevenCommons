package de.take_weiland.mods.commons;

import com.google.common.collect.ImmutableCollection;
import com.google.common.util.concurrent.ListenableFuture;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.io.Serializable;
import java.util.concurrent.Future;

import static de.take_weiland.mods.commons.asm.ASMUtils.getClassInfo;

@Mod(modid = "testmod_sc_", name = "testmod_sc_", version = "0.1")
//@NetworkMod()
public class testmod_sc_ {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		FMLInterModComms.sendMessage("sevencommons", "setUpdateUrl", "http://www.take-weiland.de/test.txt");
		print(Object.class, String.class);
		print(String.class, Object.class);
		print(Serializable.class, String.class);
		print(Serializable.class, Object.class);
		print(Serializable.class, ImmutableCollection.class);
		print(ListenableFuture.class, Future.class);
		print(Future.class, ListenableFuture.class);

	}

	private void print(Class<?> parent, Class<?> child) {
		boolean is = getClassInfo(parent).isAssignableFrom(getClassInfo(child));
		String s = is ? " instanceof " : " not instanceof ";
		System.out.print(child.getSimpleName() + s + parent.getSimpleName());
		if (is != parent.isAssignableFrom(child)) {
			System.out.println(" !!! Wrong !!!");
		} else {
			System.out.println();
		}
	}
}
