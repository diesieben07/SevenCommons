package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		Reflection.initialize(TestTe.class);
		System.exit(0);
		MinecraftForge.EVENT_BUS.register(this);

	}

	@ForgeSubscribe
	public void onPlayerInteract(EntityInteractEvent event) throws InterruptedException {
	}

	public static class TestTe extends TileEntity {

		@Sync
		private int foobar;

		@Sync
		private long foobar2;

		@Sync(syncer = TestSyncer.class)
		private String aString;
	}

	static class TestSyncer implements TypeSyncer<String> {

		@InstanceProvider
		public static TestSyncer instance() {
			return null;
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
