package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.network.PacketDownloadProgress;
import de.take_weiland.mods.commons.internal.network.PacketModState;
import de.take_weiland.mods.commons.internal.network.PacketViewUpdates;
import de.take_weiland.mods.commons.internal.updater.PlayerUpdateInformation;

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
	public void onPlayerLogout(EntityPlayer player) {
		CommonsModContainer.updateController.unregisterListener((PlayerUpdateInformation)player.getExtendedProperties(PlayerUpdateInformation.IDENTIFIER));
	}
	
	@Override
	public void handleViewUpdates(PacketViewUpdates packet) { }
	
	@Override
	public void handleModState(PacketModState packet) { }

	@Override
	public void handleDownloadProgress(PacketDownloadProgress packet) { }

	@Override
	public void onPlayerLogin(EntityPlayer player) { }

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) { }

	@Override
	public void onPlayerRespawn(EntityPlayer player) { }

}
