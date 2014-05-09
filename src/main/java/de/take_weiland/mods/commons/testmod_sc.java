package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
//		FMLInterModComms.sendMessage("sevencommons", "setUpdateUrl", "http://www.take-weiland.de/testmod.json");
		Reflection.initialize(TestTe.class);
		System.exit(0);
		MinecraftForge.EVENT_BUS.register(this);

	}

	@ForgeSubscribe
	public void onPlayerInteract(EntityInteractEvent event) throws InterruptedException {
	}

	public static class TestTe extends TileEntity {

		@ToNbt
		private String foobar = "foo";

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setBoolean("baurzus", true);
		}
	}



}
