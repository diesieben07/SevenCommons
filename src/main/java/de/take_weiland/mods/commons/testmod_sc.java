package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "required-after:sevencommons")
@NetworkMod()
public class testmod_sc {

	@Mod.Instance
	public static testmod_sc instance;

	private static Block myBlock;

	@Mod.EventHandler
	public void preInit(FMLPostInitializationEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        TestTE te = new TestTE();
//        SyncerCompanion companion = CompanionObjects.makeNewCompanion(te);
//
//        te.test = 123;
//        companion.check(te, false);
//
//        System.exit(0);

        GameRegistry.registerTileEntity(TestTE.class, "testte");
    }

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

//			@Override
//			public boolean onBlockActivated(World world, int x, int par3, int par4, EntityPlayer player, int par6, float par7, float par8, float par9) {
//				PlayerProps props = (PlayerProps) player.getExtendedProperties("testmod_sc");
//				if (Sides.logical(world).isServer()) {
//					props.someString = "clickedX = " + x;
//				} else {
//					player.addChatMessage(props.someString + " on client");
//				}
//				return true;
//			}
		};

		myBlock.setCreativeTab(CreativeTabs.tabBlock);

		GameRegistry.registerBlock(myBlock, "testblock");
		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	public void onEntityConstruct(EntityEvent.EntityConstructing event) {
		if (event.entity instanceof EntityPlayer) {
			event.entity.registerExtendedProperties("testmod_sc", new PlayerProps());
		}
	}

	private static abstract class SuperTE extends TileEntity {


	}

	public static class TestTE extends SuperTE {

        @Sync
        public String test;

        private int tick;

        @Override
        public void updateEntity() {
            if (tick++ % 10 == 0) {
                if (Sides.logical(this).isServer()) {
                    test = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
                } else {
                    System.out.println("client val is " + test);
                }
            }
        }
    }

	private static class PlayerProps implements IExtendedEntityProperties {

		@ToNbt
		String someString;

        @ToNbt
        int read;

		@Override
		public void saveNBTData(NBTTagCompound compound) {

		}

		@Override
		public void loadNBTData(NBTTagCompound compound) {

		}

		@Override
		public void init(Entity entity, World world) {

		}
	}

}
