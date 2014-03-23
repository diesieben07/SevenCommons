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
			new SomeResponsePacket(125).sendToAllTracking(event.target).onResponse(new PacketResponseHandler<String>() {
				@Override
				public void onResponse(String response, EntityPlayer responder) {
					System.out.println(responder + " send '" + response + "'");
				}
			});
		}
	}


	private static enum MyPackets implements SimplePacketType {
		HAS_RESPONSE(SomeResponsePacket.class);

		private MyPackets(Class<? extends ModPacket> packet) {
			this.packet = packet;
		}

		private final Class<? extends ModPacket> packet;

		@Override
		public Class<? extends ModPacket> packet() {
			return packet;
		}
	}

	private static class SomeResponsePacket extends ModPacket.WithResponse<String> {

		private int value;

		SomeResponsePacket(int value) {
			this.value = value;
		}

		@Override
		protected void write(WritableDataBuf buffer) {
			buffer.putInt(value);
		}

		@Override
		protected void handle(DataBuf in, EntityPlayer player, Side side, WritableDataBuf out) {
			value = in.getInt();
			out.putString(player.username + "@" + value);
		}

		@Override
		public String readResponse(DataBuf in, EntityPlayer player, Side side) {
			return in.getString();
		}

		@Override
		protected boolean validOn(Side side) {
			return side.isClient();
		}
	}


}
