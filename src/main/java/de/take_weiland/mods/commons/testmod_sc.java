package de.take_weiland.mods.commons;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import org.jetbrains.annotations.NotNull;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	public static void main(String[] args) {

	}


	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		new Base();
	}

	public static class Base extends TileEntity {

		@ToNbt
		private int foobar;

		@ToNbt(onMissing = ToNbt.ValueMissingAction.USE_DEFAULT)
		private String ggogo = "hello";

		@ToNbt
		@NotNull
		private NBTTagCompound nbt = new NBTTagCompound();

		@ToNbt
		@NotNull
		private ForgeDirection dir = ForgeDirection.DOWN;

	}

}
