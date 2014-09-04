package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.sync.Sync;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		new Test();
	}

	public static class Base implements IExtendedEntityProperties {

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

	public static class Test extends Base {

		@Sync
		@NotNull
		private	EnumSet<ForgeDirection> enumSet0;

		@NotNull
		private	EnumSet<ForgeDirection> enumSet;


		@NotNull
		@Sync
		public EnumSet<ForgeDirection> getEnumSet() {
			return enumSet;
		}

		public void setEnumSet(@NotNull EnumSet<ForgeDirection> enumSet) {
			this.enumSet = enumSet;
		}
	}


}
