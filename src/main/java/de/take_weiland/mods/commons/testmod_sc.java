package de.take_weiland.mods.commons;

import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketTarget;
import de.take_weiland.mods.commons.net.Packets;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.sync.Synced;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.trait.*;
import de.take_weiland.mods.commons.util.Listenable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.io.Serializable;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1")
@NetworkMod()
public class testmod_sc {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) throws Exception {
		NetworkRegistry.instance().registerGuiHandler(this, new IGuiHandler() {
			@Override
			public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				return new TestContainer();
			}

			@Override
			public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				return new TestGuiContainer((Container) getServerGuiElement(ID, player, world, x, y, z));
			}
		});

		GameRegistry.registerPlayerTracker(new IPlayerTracker() {
			@Override
			public void onPlayerLogin(EntityPlayer player) {
				player.openGui(testmod_sc.this, 0, player.worldObj, 0, 0, 0);
			}

			@Override
			public void onPlayerLogout(EntityPlayer player) { }

			@Override
			public void onPlayerChangedDimension(EntityPlayer player) { }

			@Override
			public void onPlayerRespawn(EntityPlayer player) { }
		});

		new TestExtendedProperties();

		new Use().foo();
	}

	@Trait(impl = testmod_sc.Impl.class)
	private static interface ATrait {

		void foo();

	}

	static class Impl implements TraitImpl, ATrait {

		@Instance
		private Serializable self;

		@Override
		public void foo() {
			test(getString());
		}

		private String getString() {
			return "foo";
		}

		private void test(String bar) {
			System.out.println(bar);
		}
	}

	@HasTrait
	static class Use implements ATrait {

		@Override
		@TraitMethod
		public void foo() { }
	}

	@HasTrait()
	static class UseSub implements Listenable<UseSub> {

		@Override
		@TraitMethod
		public void registerListener(Listener<? super UseSub> listener) { }

		@Override
		@TraitMethod
		public void removeListener(Listener<? super UseSub> listener) { }

		@Override
		@TraitMethod
		public void onChange() { }

	}

	@Synced
	private static class TestExtendedProperties implements IExtendedEntityProperties {

		@Synced
		private int foo;

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

	private static class TestGuiContainer extends GuiContainer {

		public TestGuiContainer(Container c) {
			super(c);
		}

		@Override
		protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
			System.out.println(((TestContainer) inventorySlots).sync);
			drawBackground(0);
		}
	}

	@Synced
	private static class TestContainer extends Container {

		public TestContainer(String test) {
			this();
		}

		public TestContainer() {

		}


		private int sync = 9;

		@Synced(target = TestPacketTarget.class, setter = "setter")
		private int getter() {
			return sync;
		}

		@Synced.Setter("setter")
		private void setter(int sync) {
			this.sync = sync;
		}

		@Override
		public void detectAndSendChanges() {
			super.detectAndSendChanges();
			sync++;
		}
		//
//		@Synced(target = TestPacketTarget.class)
//		private String foobar;
//
//		@Synced
//		private Object foobarusMaximus;

		@Override
		public boolean canInteractWith(EntityPlayer entityplayer) {
			return true;
		}
	}

	public static class TestPacketTarget implements PacketTarget {

		private TestContainer c;

		public TestPacketTarget(Object o) {
			c =(TestContainer) o;
		}

		@Override
		public void send(Packet packet) {
			Packets.sendPacketToViewing(packet, c);
		}
	}

	public static class TestSyncer implements TypeSyncer<Object> {

		public TestSyncer(Object instance) {

		}

		@Override
		public boolean equal(Object now, Object prev) {
			return false;
		}

		@Override
		public void write(Object instance, WritableDataBuf out) {

		}

		@Override
		public Object read(Object oldInstance, DataBuf in) {
			return null;
		}
	}
}
