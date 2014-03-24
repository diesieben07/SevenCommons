package de.take_weiland.mods.commons.internal.client;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.internal.SevenCommonsProxy;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;

public class ClientProxy implements SevenCommonsProxy {

	private final Minecraft mc = Minecraft.getMinecraft();
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@ForgeSubscribe
	public void onGuiInit(GuiInitEvent event) {
		if (SCModContainer.updaterEnabled && event.gui instanceof GuiMainMenu) {
			event.buttons.add(new GuiButtonUpdates(-1, event.gui.width / 2 + 104, event.gui.height / 4 + 48 + 72 + 12));
		}
	}

	@Override
	public void handleViewUpdates(UpdateController controller) {
		mc.displayGuiScreen(new GuiNewUpdates(null, controller));
	}

	@Override
	public void shutdownMinecraft() {
		mc.shutdown();
	}

	@Override
	public void displayRestartFailure() {
		if (mc.currentScreen instanceof GuiNewUpdates) {
			mc.displayGuiScreen(new GuiRestartFailure(mc.currentScreen));
		}
	}

	@Override
	public void refreshUpdatesGui() {
		if (mc.currentScreen instanceof GuiNewUpdates) {
			((GuiNewUpdates) mc.currentScreen).updateButtonState();
		}
	}

	@Override
	public INetworkManager getNetworkManagerFromClient(NetHandler clientHandler) {
		return ((NetClientHandler)clientHandler).getNetManager();
	}

	@Override
	public void sendPacketToServer(Packet p) {
		mc.getNetHandler().addToSendQueue(p);
	}
}
