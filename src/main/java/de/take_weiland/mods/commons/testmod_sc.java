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

import static java.util.concurrent.TimeUnit.SECONDS;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		FMLInterModComms.sendMessage("sevencommons", "setUpdateUrl", "http://www.take-weiland.de/testmod.json");

		MinecraftForge.EVENT_BUS.register(this);

		Network.simplePacketHandler("testmodsc", MyPackets.class);
	}

	@ForgeSubscribe
	public void onPlayerInteract(EntityInteractEvent event) throws InterruptedException {
		if (Sides.logical(event.entity).isClient()) {
			new MyRequestPacket(15).onResponse(new PacketResponseHandler<MyResponsePacket>() {
				@Override
				public void onResponse(MyResponsePacket packet, EntityPlayer responder) {
					System.out.println("received: " + packet.getResponse());
				}
			}).waitAtMost(15, SECONDS).sendToServer();
		}
	}

	static enum MyPackets implements SimplePacketType {

		REQUEST(MyRequestPacket.class),
		RESPONSE(MyResponsePacket.class);

		private final Class<? extends ModPacketBase> packet;

		private MyPackets(Class<? extends ModPacketBase> packet) {
			this.packet = packet;
		}


		@Override
		public Class<? extends ModPacketBase> packet() {
			return packet;
		}
	}

	static class MyRequestPacket extends ModPacket.WithResponse<MyResponsePacket> {

		private int request;

		MyRequestPacket(int request) {
			this.request = request;
		}

		@Override
		protected void write(WritableDataBuf buffer) {
			buffer.putInt(request);
		}

		@Override
		protected MyResponsePacket handle(DataBuf in, EntityPlayer player, Side side) {
			int request = in.getInt();
			return new MyResponsePacket(request + 5);
		}

		@Override
		protected boolean validOn(Side side) {
			return side.isServer();
		}

		@Override
		protected Class<MyResponsePacket> responseClass() {
			return MyResponsePacket.class;
		}

	}

	static class MyResponsePacket extends ModPacket.Response {

		private int response;

		MyResponsePacket(int response) {
			this.response = response;
		}

		@Override
		protected void write(WritableDataBuf buffer) {
			buffer.putInt(response);
		}

		@Override
		protected void handle(DataBuf buffer, EntityPlayer player, Side side) {
			response = buffer.getInt();
		}

		@Override
		protected boolean validOn(Side side) {
			return side.isClient();
		}

		public int getResponse() {
			return response;
		}
	}

}
