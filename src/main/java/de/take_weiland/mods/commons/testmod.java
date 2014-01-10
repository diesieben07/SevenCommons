package de.take_weiland.mods.commons;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.sync.Synced;

@Mod(modid = "testmod", name = "testmod", version = "0.1")
@NetworkMod()
public class testmod {

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		NetworkRegistry.instance().registerGuiHandler(this, new IGuiHandler() {
			
			@Override
			public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				return new TestContainer();
			}
			
			@Override
			public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		GameRegistry.registerPlayerTracker(new IPlayerTracker() {
			
			@Override
			public void onPlayerRespawn(EntityPlayer player) { }
			
			@Override
			public void onPlayerLogout(EntityPlayer player) { }
			
			@Override
			public void onPlayerLogin(EntityPlayer player) {
				player.openGui(testmod.this, 0, player.worldObj, 0, 0, 0);
			}
			
			@Override
			public void onPlayerChangedDimension(EntityPlayer player) { }
		});
	}
	
	@Synced
	private static class TestContainer extends Container {

		@Synced
		private boolean synced = false;
		
		@Override
		public boolean canInteractWith(EntityPlayer entityplayer) {
			return true;
		}
		
		
		
		
	}

}
