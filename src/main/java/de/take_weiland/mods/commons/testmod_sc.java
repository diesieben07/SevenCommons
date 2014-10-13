package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.sync.SyncContents;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

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
		};
		GameRegistry.registerBlock(myBlock, "testblock");
		new TestTE();

	}

	private static class BaseTE extends TileEntity {

	}

	private static class TestTE extends BaseTE {

		@SyncContents
		private FluidTank tank = new FluidTank(30);

		private int ticks = 0;
		@Override
		public void updateEntity() {
			if (ticks++ % 20 == 0) {
				if (Sides.logical(this).isServer()) {
					if (tank.getFluid() == null || tank.getFluid().getFluid() == FluidRegistry.LAVA) {
						tank.setFluid(new FluidStack(FluidRegistry.WATER, 10));
					} else {
						tank.setFluid(new FluidStack(FluidRegistry.LAVA, 10));
					}
				}
				System.out.println("Fluid is " + (tank.getFluid() == null ? null : tank.getFluid().getFluid().getLocalizedName()) + " on " + Sides.logical(this));
			}
		}
	}


}
