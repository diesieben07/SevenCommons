package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import de.take_weiland.mods.commons.sync.Sync;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

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

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) throws NoSuchMethodException, NoSuchFieldException, IOException {
		new TestTE();
		new ExtendedProps();
		System.exit(0);
	}

	public static abstract class BaseProps implements IExtendedEntityProperties {

		@Sync
		private String stringInBase;

	}

	public static final class ExtendedProps extends BaseProps implements IExtendedEntityProperties {

		@Sync
		private String s;

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
