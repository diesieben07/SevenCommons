package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.io.IOException;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "after:SevenCommons")
@NetworkMod()
public class testmod_sc {

	private static Enum enumSet;

	public static void main(@Nonnull String[] bar) throws NoSuchFieldException {
	}

	@Mod.Instance
	public static testmod_sc instance;

	private static Block myBlock;

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) throws NoSuchMethodException, NoSuchFieldException, IOException {
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

		myBlock.setCreativeTab(CreativeTabs.tabBlock);

		GameRegistry.registerBlock(myBlock, "testblock");

	}

}
