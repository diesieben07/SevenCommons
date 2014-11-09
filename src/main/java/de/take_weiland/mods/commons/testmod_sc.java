package de.take_weiland.mods.commons;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.client.Rendering;
import de.take_weiland.mods.commons.inv.BasicSlot;
import de.take_weiland.mods.commons.inv.ButtonContainer;
import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.inv.Inventories;
import de.take_weiland.mods.commons.tileentity.TileEntityInventory;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
@NetworkMod()
public class testmod_sc {

	public static void main(@Nonnull String[] bar) {
		System.out.println(String[][].class.getName());
		System.out.println(JavaUtils.hasUnsafe());
		System.out.println(JavaUtils.getUnsafe());
	}

	@Mod.Instance
	public static testmod_sc instance;

	private static Block myBlock;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws NoSuchMethodException, NoSuchFieldException, IOException {
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
				if (Sides.logical(par1World).isClient()) {
					par5EntityPlayer.openGui(testmod_sc.instance, 0, par1World, par2, par3, par4);
				}
				return true;
			}
		};

		try {
			ItemStack stone = new ItemStack(Block.stone);
			NBTTagList list = new NBTTagList();
			NBTTagCompound stoneTag = stone.writeToNBT(new NBTTagCompound());
			stoneTag.setFloat("slot", (byte) 5);
			list.appendTag(stoneTag);

			ItemStack[] inv = new ItemStack[3];
			Inventories.readInventory(inv, list);
		} catch (ReportedException e) {
			Files.asCharSink(new File("test.txt"), Charsets.UTF_8).write(e.getCrashReport().getCompleteReport());
		}
		System.exit(0);

		Reflection.initialize(GuiContainer.class);
//		System.exit(0);
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

	public static class TestContainer extends Container implements ButtonContainer {

		public TestContainer(TestTE te, InventoryPlayer playerInv) {
			for (int i = 0; i < 5; i++) {
				addSlotToContainer(new BasicSlot(te, i, i * 20 + 4, 20));
			}
			Containers.addPlayerInventory(this, playerInv);
		}

		@Override
		public ItemStack transferStackInSlot(@Nonnull EntityPlayer player, int slotId) {
			return Containers.handleShiftClick(this, player, slotId);
		}

		@Override
		public boolean canInteractWith(EntityPlayer entityplayer) {
			return true;
		}

		@Override
		public void onButtonClick(Side side, EntityPlayer player, int buttonId) {

		}

	}

	public static class TestGui extends GuiScreen {


		@Override
		public void drawScreen(int par1, int par2, float par3) {
			super.drawScreen(par1, par2, par3);
			mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			Rendering.fillAreaWithIcon(FluidRegistry.WATER.getIcon(), 10, 10, 31, 100);
		}
	}

	public static class TestGuiHandler implements IGuiHandler {

		@Override
		public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//			System.out.println("hello");
//			return new TestContainer((TestTE) world.getBlockTileEntity(x, y, z), player.inventory);
			return null;
		}

		@Override
		public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
			return new TestGui();
		}
	}


}
