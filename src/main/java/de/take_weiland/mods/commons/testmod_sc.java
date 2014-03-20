package de.take_weiland.mods.commons;

import com.google.common.collect.ImmutableCollection;
import com.google.common.util.concurrent.ListenableFuture;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.net.ClientResponseHandler;
import de.take_weiland.mods.commons.net.PacketResponseHandler;
import de.take_weiland.mods.commons.net.SimplePacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.io.Serializable;
import java.util.concurrent.Future;

import static de.take_weiland.mods.commons.asm.ASMUtils.getClassInfo;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		FMLInterModComms.sendMessage("sevencommons", "setUpdateUrl", "http://www.take-weiland.de/testmod.json");
		print(Object.class, String.class);
		print(String.class, Object.class);
		print(Serializable.class, String.class);
		print(Serializable.class, Object.class);
		print(Serializable.class, ImmutableCollection.class);
		print(ListenableFuture.class, Future.class);
		print(Future.class, ListenableFuture.class);

		EntityPlayer player = null;
		TileEntity te = null;

		get().sendTo(player).onResponse(new PacketResponseHandler<String>() {
			@Override
			public void onResponse(String response, EntityPlayer responder) {
				System.out.println(response);
			}
		}).sendToServer().discardResponse()
		.sendToAllTracking(te).onResponse(new PacketResponseHandler<String>() {
			@Override
			public void onResponse(String response, EntityPlayer responder) {
				System.out.println(response);
			}
		}).sendToServer().onResponse(new ClientResponseHandler<String>() {
			@Override
			public void onResponse(String response) {

			}
		});
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

	private SimplePacket.WithResponse<String> get() {
		return null;
	}
}
