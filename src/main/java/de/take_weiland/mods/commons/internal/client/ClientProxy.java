package de.take_weiland.mods.commons.internal.client;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.client.ScreenWithParent;
import de.take_weiland.mods.commons.internal.SevenCommonsProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;

public final class ClientProxy implements SevenCommonsProxy {

	private final Minecraft mc = Minecraft.getMinecraft();

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	public void onOpenGui(GuiOpenEvent event) {
		if (event.gui == null && mc.currentScreen instanceof ScreenWithParent) {
			event.gui = ((ScreenWithParent) mc.currentScreen).getParentScreen();
		}
	}

	@Override
	public INetworkManager getNetworkManagerFromClient(NetHandler clientHandler) {
		return ((NetClientHandler) clientHandler).getNetManager();
	}

	@Override
	public void sendPacketToServer(Packet p) {
		mc.getNetHandler().addToSendQueue(p);
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return mc.thePlayer;
	}

	@Override
	public String translate(String key) {
		return I18n.getString(key);
	}
}
