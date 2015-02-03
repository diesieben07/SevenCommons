package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.serialize.MethodPair;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

import static java.lang.invoke.MethodType.methodType;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "required-after:sevencommons")
@NetworkMod()
public class testmod_sc {

    private String enumSet;

	public static void main(@Nonnull String[] bar) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle stringReader = lookup.findStatic(testmod_sc.class, "doRead", methodType(void.class, NBTBase.class, String.class));

        MethodHandle getter = lookup.findVirtual(testmod_sc.class, "doGet", methodType(String.class));
        MethodHandle stringWriter = lookup.findConstructor(NBTTagString.class, methodType(void.class, String.class, String.class));
        stringWriter = MethodHandles.insertArguments(stringWriter, 0, "");

        MethodHandle mh = MethodPair.makeWriter(stringWriter, getter);

        testmod_sc inst = new testmod_sc();
        inst.enumSet = "hello world";
        System.out.println((NBTBase) mh.invokeExact(inst));
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
	public void preInit(FMLPreInitializationEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Reflection.initialize(TestTE.class);
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

		@ToNbt
		private TileEntity[][] helloWorld;

	}

	private static class TestTE extends SuperTE {

		@ToNbt
		private int[][] tank;

		void foo() {

		}

	}

	private static class PlayerProps implements IExtendedEntityProperties {

		@ToNbt
		String someString;

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
