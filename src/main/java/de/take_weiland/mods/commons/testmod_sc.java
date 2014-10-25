package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.inv.BasicSlot;
import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.tileentity.TileEntityInventory;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
@NetworkMod()
public class testmod_sc {

	@Mod.Instance
	public static testmod_sc instance;

	private static Block myBlock;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws NoSuchMethodException, NoSuchFieldException {
		myBlock = new Block(4000, Material.rock) {

			@Override
			public boolean hasTileEntity(int metadata) {
				return true;
			}

			@Override
			public TileEntity createTileEntity(World world, int metadata) {
				return new TestTE();
			}

			@Override
			public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
				if (Sides.logical(par1World).isServer()) {
					System.out.println("hello");
					par5EntityPlayer.openGui(testmod_sc.instance, 0, par1World, par2, par3, par4);
				}
				return true;
			}
		};
		GameRegistry.registerTileEntity(TestTE.class, "testte");
		GameRegistry.registerBlock(myBlock, "testblock");
		NetworkRegistry.instance().registerGuiHandler(this, new TestGuiHandler());

	}

	public static class TestTE extends TileEntityInventory {

		@Override
		public void updateEntity() {
		}

		@Override
		protected String unlocalizedName() {
			return "foo.bar";
		}

		@Override
		public int getSizeInventory() {
			return 5;
		}
	}

	public static class TestContainer extends Container {

		public TestContainer(TestTE te, InventoryPlayer playerInv) {
			for (int i = 0; i < 5; i++) {
				addSlotToContainer(new BasicSlot(te, i, i * 20 + 4, 20));
			}
			Containers.addPlayerInventory(this, playerInv);
		}

		@Override
		public ItemStack transferStackInSlot(EntityPlayer player, int slotId) {
			return Containers.handleShiftClick(this, player, slotId);
		}

		@Override
		public boolean canInteractWith(EntityPlayer entityplayer) {
			return true;
		}
	}

	public static class TestGui extends GuiContainer {

		public TestGui(Container container) {
			super(container);
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
			drawDefaultBackground();
		}
	}

	public static class TestGuiHandler implements IGuiHandler {

		@Override
		public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
			System.out.println("hello");
			return new TestContainer((TestTE) world.getBlockTileEntity(x, y, z), player.inventory);
		}

		@Override
		public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
			return new TestGui(getServerGuiElement(ID, player, world, x, y, z));
		}
	}


}
