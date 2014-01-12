package de.take_weiland.mods.commons;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
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
				return new TestGuiContainer((Container) getServerGuiElement(ID, player, world, x, y, z));
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
	static class TestContainer extends Container {

		@Synced
		private boolean synced = false;
		
		@Synced
		private int testus = -3;
		
		
		@Override
		public boolean canInteractWith(EntityPlayer entityplayer) {
			int a = 5;
			switch (a) {
			case -1:
				break;
			case 0:
				break;
			case 1:
				break;
			}
			return true;
		}

		@Override
		public void detectAndSendChanges() {
			super.detectAndSendChanges();
			synced = !synced;
		}
		
		
	}
	
	static class TestGuiContainer extends GuiContainer {

		public TestGuiContainer(Container par1Container) {
			super(par1Container);
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
			drawBackground(0);
		}

		@Override
		public void updateScreen() {
			super.updateScreen();
		}
		
		
		
	}

}
