package de.take_weiland.mods.commons;

import com.google.common.reflect.Reflection;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketTarget;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;

import java.lang.annotation.ElementType;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
//@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		Reflection.initialize(Test.class, Container.class);
		System.exit(0);
	}

	public static class Test extends TileEntity {

		@Sync
		private ItemStack foobar;

		@Sync
		private long foobar2;

		@Sync(target = TestPacketTarget.class)
		private ElementType bla;

		@Sync(syncer = TestSyncer.class)
		private String aString;

	}

	static class TestSyncer implements TypeSyncer<String> {

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

	static class TestPacketTarget implements PacketTarget {

		@Override
		public void send(Packet packet) {

		}
	}



}
