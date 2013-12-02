package de.take_weiland.mods.commons.internal.client;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.internal.CommonsModContainer;
import de.take_weiland.mods.commons.internal.PacketDownloadProgress;
import de.take_weiland.mods.commons.internal.PacketModState;
import de.take_weiland.mods.commons.internal.PacketViewUpdates;
import de.take_weiland.mods.commons.internal.SevenCommonsProxy;
import de.take_weiland.mods.commons.internal.updater.UpdateControllerRemote;

public class ClientProxy implements SevenCommonsProxy {

	private final Minecraft mc = Minecraft.getMinecraft();
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@ForgeSubscribe
	public void onGuiInit(GuiInitEvent event) {
		if (CommonsModContainer.updaterEnabled && event.gui instanceof GuiMainMenu) {
			event.buttons.add(new GuiButtonUpdates(-1, event.gui.width / 2 + 104, event.gui.height / 4 + 48 + 72 + 12));
		}
	}

	@Override
	public void handleViewUpdates(PacketViewUpdates packet) {
		mc.displayGuiScreen(new GuiUpdates(null, new UpdateControllerRemote(packet.getMods())));
	}

	@Override
	public void handleModState(PacketModState packet) {
		if (mc.currentScreen instanceof GuiUpdates) {
			((GuiUpdates) mc.currentScreen).controller.getMod(packet.getModId()).transition(packet.getState());
		}
	}

	@Override
	public void handleDownloadProgress(PacketDownloadProgress packet) {
		if (mc.currentScreen instanceof GuiUpdates) {
			((GuiUpdates) mc.currentScreen).controller.getMod(packet.getModId()).setDownloadProgress(packet.getDownloadProgress(), 100);
		}
	}

	@Override
	public void shutdownMinecraft() {
		mc.shutdown();
	}

	@Override
	public void displayRestartFailure() {
		if (mc.currentScreen instanceof GuiUpdates) {
			mc.displayGuiScreen(new GuiRestartFailure(mc.currentScreen));
		}
	}

	@Override
	public INetworkManager getNetworkManagerFromClient(NetHandler clientHandler) {
		return ((NetClientHandler)clientHandler).getNetManager();
	}

}
