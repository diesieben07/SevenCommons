package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
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
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "required-after:sevencommons")
@NetworkMod()
public class testmod_sc {

	@ToNbt(key = "helloWorld")
	private String enumSet;

	public static void main(@Nonnull String[] bar) throws NoSuchFieldException {
	}

	@Mod.Instance
	public static testmod_sc instance;

	private static Block myBlock;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//		Watcher<?> watcher = SerializerRegistry.getWatcher(new TypeSpecification.Predefined<>(BitSet.class, SerializationMethod.DEFAULT));
//		System.out.println(JavaUtils.defaultToString(watcher));
		testmod_sc.class.getDeclaredMethod("_sc$tonbt", NBTTagCompound.class).invoke(this, new NBTTagCompound());

		Reflection.initialize(PlayerProps.class);
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

	private static class PlayerProps implements IExtendedEntityProperties {

		@Sync
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
