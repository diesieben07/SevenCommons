package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import com.google.common.reflect.TypeToken;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.SyncCapacity;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.FieldContext;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.EnumSet;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
@NetworkMod()
public class testmod_sc {

	private EnumSet<ForgeDirection> sets;
	private EnumSet<ElementType> set2;

	@SyncCapacity
	private FluidTank tank;

	public static void main(@Nonnull String[] bar) throws NoSuchFieldException {
		System.out.println(TypeToken.of(testmod_sc.class.getDeclaredField("sets").getGenericType()).equals(
				new TypeToken<EnumSet<ForgeDirection>>() {}
		));
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

		System.out.println(SyncingManager.getValueSyncer(new FieldContext<>(testmod_sc.class.getDeclaredField("sets"))));
		ValueSyncer<EnumSet<ElementType>> syncer = SyncingManager.getValueSyncer(new FieldContext<EnumSet<ElementType>>(testmod_sc.class.getDeclaredField("set2")));
		System.out.println(syncer);

		System.out.println(syncer.hasChanged(set2, null));

		Reflection.initialize(TestTE.class);
		System.exit(0);
		GameRegistry.registerTileEntity(TestTE.class, "testte");
		GameRegistry.registerBlock(myBlock, "testblock");

	}

	public static class TestTE extends TileEntity {

		@Sync
		private ItemStack stack;

		@Override
		public void updateEntity() {
		}

	}


}
