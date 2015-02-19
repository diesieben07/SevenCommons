package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "required-after:sevencommons")
@NetworkMod()
public class testmod_sc {

    private String enumSet;

	public static void main(@Nonnull String[] bar) throws Throwable {
//        MethodHandles.Lookup lookup = MethodHandles.lookup();
//
//        String name = "enumSet";
//        MethodType type = methodType(NBTBase.class, testmod_sc.class);
//
//        MethodHandle get = lookup.findGetter(testmod_sc.class, "enumSet", String.class);
//        MethodHandle set = lookup.findSetter(testmod_sc.class, "enumSet", String.class);
//
//        testmod_sc inst = new testmod_sc();
//        inst.enumSet = "hello";
//
////        CallSite callSite = ToNbtBootstrap.makeNBTWrite(lookup, name, type, 0, get, set);
//        System.out.println(JavaUtils.defaultToString(callSite.getTarget().invoke(inst)));
    }

    private static void doRead(NBTBase nbt, String s) {
        System.out.println("reading now! " + nbt + ", old val " + s);
    }

    private String doGet() {
        System.out.println("getting!");
        return enumSet;
    }

	@Mod.Instance
	public static testmod_sc instance;

	private static Block myBlock;

	@Mod.EventHandler
	public void preInit(FMLPostInitializationEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        GameRegistry.registerTileEntity(TestTE.class, "testtile");
        NBTTagCompound nbt = new NBTTagCompound();
        TestTE testTE = new TestTE();
        testTE.list = new ArrayList<>();
        testTE.list.add(Arrays.asList("foo", "bar"));
        testTE.list.add(Arrays.asList("hello", "world"));

        testTE.writeToNBT(nbt);

        testTE.list = null;

        testTE.readFromNBT(nbt);

        System.out.println(testTE.list);

        System.exit(0);
    }

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) throws NoSuchMethodException, NoSuchFieldException, IOException {
		myBlock = new Block(4000, Material.rock) {

//			@Override
//			public boolean hasTileEntity(int metadata) {
//				return true;
//			}
//
//			@Override
//			public TileEntity createTileEntity(World world, int metadata) {
//				return new TestTE();
//			}

			@Override
			public boolean onBlockActivated(World world, int x, int par3, int par4, EntityPlayer player, int par6, float par7, float par8, float par9) {
				PlayerProps props = (PlayerProps) player.getExtendedProperties("testmod_sc");
				if (Sides.logical(world).isServer()) {
					props.someString = "clickedX = " + x;
				} else {
					player.addChatMessage(props.someString + " on client");
				}
				return true;
			}
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

	private static class TestTE extends SuperTE {

//		@ToNbt
		private int[] tank;

        @ToNbt
        List<List<String>> list;

        @Override
        public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
        }

        @Override
        public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
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
