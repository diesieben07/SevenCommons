package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
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

		final NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < 5; ++i) {
			list.appendTag(new NBTTagByteArray("", new byte[] { 1, 2, 4, 7 }));
		}
		nbt.setTag("moreDims", list);

//		TestTe t = new TestTe();
//		t.readFromNBT(nbt);
//		System.out.println(Arrays.deepToString(t.moreDims));

		System.exit(0);
		MinecraftForge.EVENT_BUS.register(this);

	}

	@ForgeSubscribe
	public void onPlayerInteract(EntityInteractEvent event) throws InterruptedException {
	}

	public static class TestTe extends TileEntity {

		@ToNbt
		private ForgeDirection[][][] foobar;

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			System.out.println("writeToNBT!");
		}

		@Override
		public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
			super.readFromNBT(par1NBTTagCompound);
			System.out.println("readFromNBT!");
		}
	}



}
