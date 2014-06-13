package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

import java.lang.annotation.ElementType;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		Reflection.initialize(Test.class, Container.class);
		System.exit(0);
		MinecraftForge.EVENT_BUS.register(this);

		boolean a = false;
		boolean b = true;

		System.out.println(a == b);
		Object o = null;
		while (true) {
			switch (o.hashCode()) {
				case -1:
					return;
				case 0:
					// todo
					break;
				case 1:
					// todo:
					break;
				default:
					System.out.println("Invalid index!");
					break;
			}
		}
	}

	@ForgeSubscribe
	public void onPlayerInteract(EntityInteractEvent event) throws InterruptedException {
	}

	private static abstract class Base implements IExtendedEntityProperties {

		@Sync
		private ItemStack garumP;

	}

	public static class Test extends Base {

		@Sync
		private ItemStack foobar;

		@Sync
		private long foobar2;

		@Sync
		private ElementType bla;

		@Sync
		private String aString;

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

	static class TestSyncer implements TypeSyncer<String> {

//		@InstanceProvider
//		public static TestSyncer instance(TestTe tile) {
//			return null;
//		}

		TestSyncer(Test te) {

		}

		@Override
		public boolean equal(String now, String prev) {
			return false;
		}

		@Override
		public void write(String instance, WritableDataBuf out) {

		}

		@Override
		public String read(String oldInstance, DataBuf in) {
			return null;
		}
	}



}
