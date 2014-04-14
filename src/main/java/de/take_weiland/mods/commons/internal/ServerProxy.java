package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.updater.PlayerUpdateInformation;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

public class ServerProxy implements SevenCommonsProxy, IPlayerTracker {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		GameRegistry.registerPlayerTracker(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@ForgeSubscribe
	public void onEntityConstruct(EntityEvent.EntityConstructing event) {
		if (event.entity instanceof EntityPlayer) {
			event.entity.registerExtendedProperties(PlayerUpdateInformation.IDENTIFIER, new PlayerUpdateInformation());
		}
	}

	@Override
	public void handleViewUpdates(UpdateController controller) { }

	@Override
	public void onPlayerLogout(EntityPlayer player) {
	}
	
	@Override
	public void onPlayerLogin(EntityPlayer player) { }

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) { }

	@Override
	public void onPlayerRespawn(EntityPlayer player) { }

	@Override
	public void shutdownMinecraft() {
		MinecraftServer.getServer().initiateShutdown();
	}

	@Override
	public INetworkManager getNetworkManagerFromClient(NetHandler clientHandler) {
		throw new IllegalStateException("NetHandler.isServerHandler() should always be true on a dedicated server!");
	}

	@Override
	public void sendPacketToServer(Packet p) {
		throw new IllegalStateException("Server cannot send Packet to itself!");
	}

	@Override
	public void displayUpdateGui(UpdateController controller) {
	}

	public static EntityPlayer currentUpdateViewer;
	public static int lastPercent;

	@Override
	public void displayRestartFailure() {
		if (currentUpdateViewer != null) {
			new PacketClientAction(PacketClientAction.Action.RESTART_FAILURE).sendTo(currentUpdateViewer);
		}
	}

	@Override
	public void displayOptimizeFailure() {
		if (currentUpdateViewer != null) {
			new PacketClientAction(PacketClientAction.Action.OPTIMIZE_FAILURE).sendTo(currentUpdateViewer);
		}
	}

	@Override
	public void refreshUpdatesGui() {
		if (currentUpdateViewer != null) {
			new PacketDisplayUpdates().sendTo(currentUpdateViewer);
		}
	}

	@Override
	public void handleVersionSelect(String modId, int index) {
		if (currentUpdateViewer != null) {
			UpdatableMod mod = SCModContainer.updateController.getMod(modId);
			if (mod != null) {
				mod.getVersions().selectVersion(index);
			}
		}
	}

	@Override
	public void handleDownloadPercent(int percent) { }

	public static void resetUpdateViewer(Object player) {
		if (currentUpdateViewer == player) {
			currentUpdateViewer = null;
			lastPercent = -1;
		}
	}
}
