package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.sync.Sync;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.BitSet;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	public static void main(String[] args) {

	}

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

		Reflection.initialize(TestTE.class);
		System.exit(0);
	}

	private static class BaseTE extends TileEntity {

	}

	private static class TestTE extends BaseTE {

		@Sync
		private BitSet set;

		@Sync
		private Boolean foobar;

		private int ticks = 0;
		@Override
		public void updateEntity() {
		}
	}


}
