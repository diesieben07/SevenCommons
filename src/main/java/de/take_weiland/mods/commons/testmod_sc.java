package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	static PacketFactory<MyPackets> packets;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		FMLInterModComms.sendMessage("sevencommons", "setUpdateUrl", "http://www.take-weiland.de/testmod.json");
		packets = Network.simplePacketHandler("testmodsc", MyPackets.class);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	public void onPlayerInteract(EntityInteractEvent event) {
		if (Sides.logical(event.entity).isServer()) {
			new MyResponsePacket("foobar").onResponse(new SimplePacketResponseHandler<String>() {
				@Override
				public void onResponse(String response, EntityPlayer responder) {
					System.out.println("received: " + response);
				}
			}).sendTo(event.entityPlayer);
		}
	}

	static enum MyPackets implements SimplePacketType {

		HAS_RESPONSE;

		@Override
		public Class<? extends ModPacket> packet() {
			return MyResponsePacket.class;
		}


	}

	static class MyResponsePacket extends ModPacket.WithResponse<String> {

		private String data;

		MyResponsePacket(String data) {
			this.data = data;
		}

		@Override
		protected void handle(DataBuf in, EntityPlayer player, Side side, PacketBuilder.ForResponse response) {
			response.putString(in.getString() + "_processed!").send();
		}

		@Override
		public String readResponse(DataBuf in, EntityPlayer player, Side side) {
			return in.getString();
		}

		@Override
		protected boolean validOn(Side side) {
			return side.isClient();
		}

		@Override
		protected void write(WritableDataBuf buffer) {
			buffer.putString(data);
		}
	}



}
