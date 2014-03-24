package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.updater.PlayerUpdateInformation;
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
	public void refreshUpdatesGui() { }

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
	public void displayRestartFailure() { }

	@Override
	public INetworkManager getNetworkManagerFromClient(NetHandler clientHandler) {
		throw new IllegalStateException("NetHandler.isServerHandler() should always be true on a dedicated server!");
	}

	@Override
	public void sendPacketToServer(Packet p) {
		throw new IllegalStateException("Server cannot send Packet to itself!");
	}
}
