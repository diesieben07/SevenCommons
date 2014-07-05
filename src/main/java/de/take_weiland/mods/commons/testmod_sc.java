package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.util.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Blocks.init(new Block(3000, Material.rock), "foobar", MyItemBlock.class);
	}

	public static class MyItemBlock extends ItemBlock {

		public MyItemBlock(int par1) {
			super(par1);
		}
	}




}
