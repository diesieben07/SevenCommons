package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.event.PlayerCloneEvent;
import de.take_weiland.mods.commons.event.PlayerStartTrackingEvent;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.sync.PacketSyncProperties;
import de.take_weiland.mods.commons.internal.sync.SyncType;
import de.take_weiland.mods.commons.internal.sync.SyncableProxyInternal;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.Syncable;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A class containing methods called from ASM generated code.
 *
 * @author diesieben07
 */
public final class ASMHooks {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/ASMHooks";
	public static final String ON_START_TRACKING = "onStartTracking";
	public static final String ON_PLAYER_CLONE = "onPlayerClone";
	public static final String NEW_SYNC_STREAM = "newSyncStream";
	public static final String STACKS_EQUAL = "rawEquals";
	public static final String SEND_SYNC_PACKET = "sendSyncPacket";
	public static final String SYNC_PROXY_AS_SYNCABLE = "wrapSyncableProxy";
	public static final String BITSET_DATA_EQ = "bitSetDataEqual";
	public static final String BITSET_DATA_EQ_NOT_NULL = "bitSetDataEqualNotNull";
	public static final String BITSET_COPY_INTO = "bitSetCopyInto";
	public static final String FIND_ENUM_SET_TYPE = "findEnumSetType";
	public static final String TICK_SYNC_PROPS = "tickSyncProps";
	public static final String NEW_ENTITY_PROPS = "newEntityProps";
	public static final String ENTITY_CONSTRUCT = "entityConstructLast";

	private ASMHooks() { }

	@SuppressWarnings("ForLoopReplaceableByForEach")
	public static void tickSyncProps(List<SyncedEntityProperties> props) {
		if (props == null) {
			return;
		}
		// avoid iterator garbage
		for (int i = 0, len = props.size(); i < len; i++) {
			props.get(i)._sc$syncprops$tick();
		}
	}

	private static final Comparator<SyncedEntityProperties> SYNC_PROPS_COMP = new Comparator<SyncedEntityProperties>() {
		@Override
		public int compare(SyncedEntityProperties o1, SyncedEntityProperties o2) {
			return o1._sc$syncprops$name().compareToIgnoreCase(o2._sc$syncprops$name());
		}
	};

	public static void entityConstructLast(Entity e) {
		List<SyncedEntityProperties> synced = ((EntityProxy) e)._sc$getSyncedEntityProperties();
		if (synced != null) {
			Collections.sort(synced, SYNC_PROPS_COMP);
			for (int i = 0, len = synced.size(); i < len; i++) {
				synced.get(i)._sc$syncprops$setIndex(i);
			}
		}
	}

	public static void newEntityProps(String name, IExtendedEntityProperties props, Entity e) {
		if (props instanceof SyncedEntityProperties) {
			List<SyncedEntityProperties> synced = ((EntityProxy) e)._sc$getSyncedEntityProperties();
			if (synced == null) {
				synced = Lists.newArrayList();
				((EntityProxy) e)._sc$setSyncedEntityProperties(synced);
			}
			SyncedEntityProperties syncedProps = (SyncedEntityProperties) props;
			syncedProps._sc$syncprops$setName(name);
			syncedProps._sc$syncprops$setOwner(e);
			synced.add(syncedProps);
		}
	}

	public static Syncable wrapSyncableProxy(final SyncableProxyInternal proxy) {
		return new Syncable() {
			@Override
			public boolean needsSyncing() {
				return proxy._sc$syncable$needsSyncing();
			}

			@Override
			public void writeSyncDataAndReset(MCDataOutputStream out) {
				proxy._sc$syncable$write(out);
			}

			@Override
			public void readSyncData(MCDataInputStream in) {
				proxy._sc$syncable$read(in);
			}
		};
	}

	private static final Type iterableType = Iterable.class.getTypeParameters()[0];

	public static Class<?> findEnumSetType(Class<?> clazz, String member, boolean isMethod) {
		try {
			Type type;
			if (isMethod) {
				type = clazz.getDeclaredMethod(member).getGenericReturnType();
			} else {
				type = clazz.getDeclaredField(member).getGenericType();
			}
			// use the type parameter of Iterable, in case the Field was declared as a more generic type than EnumSet
			Class<?> setType = TypeToken.of(type).resolveType(iterableType).getRawType();
			if (!setType.isEnum()) {
				throw new RuntimeException("Parameter type of EnumSet is not an Enum?!?");
			}
			System.out.println("Determined element type of " + member + " as " + setType.getSimpleName());
			return setType;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public static boolean bitSetDataEqual(@Nullable long[] a, @Nullable BitSet bitSet) {
		if (a == null) {
			return bitSet == null;
		}
		if (bitSet == null) {
			return false;
		}

		int aLen = a.length;
		int setLen = SCReflector.instance.getWordsInUse(bitSet);
		if (aLen != setLen) {
			return false;
		}
		long[] rawSet = SCReflector.instance.getWords(bitSet);
		for (int i = 0; i < aLen; i++) {
			if (a[i] != rawSet[i]) {
				return false;
			}
		}
		return true;
	}

	public static boolean bitSetDataEqualNotNull(@Nullable long[] a, @NotNull BitSet bitSet) {
		if (a == null) {
			return false;
		}

		int aLen = a.length;
		int setLen = SCReflector.instance.getWordsInUse(bitSet);
		if (aLen != setLen) {
			return false;
		}
		long[] rawSet = SCReflector.instance.getWords(bitSet);
		for (int i = 0; i < aLen; i++) {
			if (a[i] != rawSet[i]) {
				return false;
			}
		}
		return true;
	}

	public static long[] bitSetCopyInto(BitSet from, long[] into) {
		long[] fromData = SCReflector.instance.getWords(from);
		int fromLen = SCReflector.instance.getWordsInUse(from);
		if (into.length < fromLen) {
			into = new long[fromLen];
		}
		System.arraycopy(fromData, 0, into, 0, fromLen);
		return into;
	}

	public static boolean rawEquals(ItemStack a, ItemStack b) {
		return a == b ||
				(a != null && b != null
					&& a.itemID == b.itemID
					&& SCReflector.instance.getRawDamage(a) == SCReflector.instance.getRawDamage(b)
					&& a.stackSize == b.stackSize)
					&& (a.stackTagCompound == null ? b.stackTagCompound == null : a.stackTagCompound.equals(b.stackTagCompound));
	}

	public static MCDataOutputStream newSyncStream(SyncType type, Object object) {
		MCDataOutputStream stream = SCModContainer.packets.createStream(SCModContainer.SYNC_PACKET_ID);
		stream.writeEnum(type);
		type.writeData(stream, object);
		return stream;
	}

	public static void sendSyncPacket(MCDataOutputStream stream, SyncType type, Object object) {
		type.sendPacket(SCModContainer.packets.makePacket(stream), object);
	}

	public static void onPlayerClone(EntityPlayer oldPlayer, EntityPlayer newPlayer) {
		MinecraftForge.EVENT_BUS.post(new PlayerCloneEvent(oldPlayer, newPlayer));
	}

	@SideOnly(Side.CLIENT)
	public static void onGuiInit(GuiScreen gui) {
		MinecraftForge.EVENT_BUS.post(new GuiInitEvent(gui, SCReflector.instance.getButtonList(gui)));
	}

	public static void onStartTracking(EntityPlayer player, Entity tracked) {
		MinecraftForge.EVENT_BUS.post(new PlayerStartTrackingEvent(player, tracked));
		PacketSyncProperties.sendSyncedProperties(player, tracked);
	}

	private static final int SIGNED_SHORT_BITS = 0b0111_1111_1111_1111;
	private static final int SHORT_MS_BIT = 0b1000_0000_0000_0000;

	public static void writeExtPacketLen(DataOutput out, int len) throws IOException {
		int leftover = (len & ~SIGNED_SHORT_BITS) >>> 15;
		if (leftover != 0) {
			out.writeShort(len & SIGNED_SHORT_BITS | SHORT_MS_BIT);
			out.writeByte(leftover);
		} else {
			out.writeShort(len & SIGNED_SHORT_BITS);
		}
	}

	public static int readExtPacketLen(DataInput in) throws IOException {
		int low = in.readUnsignedShort();
		if ((low & SHORT_MS_BIT) != 0) {
			int hi = in.readUnsignedByte();
			return (low & SIGNED_SHORT_BITS) | (hi << 15);
		} else {
			return low;
		}
	}

	public static int additionalPacketSize(Packet250CustomPayload packet) {
		if ((packet.length & 0b0111_1111_1000_0000_0000_0000) != 0) {
			return 1;
		} else {
			return 0;
		}
	}

	public static int additionalPacketSize(int len) {
		if ((len & 0b0111_1111_1000_0000_0000_0000) != 0) {
			return 1;
		} else {
			return 0;
		}
	}

}
