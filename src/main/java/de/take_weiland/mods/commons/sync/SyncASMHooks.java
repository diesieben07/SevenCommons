package de.take_weiland.mods.commons.sync;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.Internal;
import de.take_weiland.mods.commons.internal.CommonsModContainer;
import de.take_weiland.mods.commons.internal.SCPacket;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketBuilder;
import de.take_weiland.mods.commons.net.PacketTarget;
import de.take_weiland.mods.commons.net.Packets;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.List;

@SuppressWarnings("unused") // stuff in here gets called from ASM generated code
@Internal
public final class SyncASMHooks {

	private SyncASMHooks() { }

	public static <T> TypeSyncer<? super T> getSyncerFor(Class<T> toSync) {
		return Syncing.getSyncerFor(toSync);
	}

	private static PacketBuilder init(Object obj, PacketBuilder out, SyncType type) {
		if (out != null) {
			return out;
		}
		out = CommonsModContainer.packetFactory.builder(SCPacket.Type.SYNC);
		Packets.writeEnum(out, type);
		type.injectInfo(obj, out);
		return out;
	}

	private static PacketBuilder idxNull(Object obj, PacketBuilder out, int idx, SyncType type) {
		out = init(obj, out, type);
		out.putByte((idx & 0x3f) | 0x40); // (idx & 0011 1111) | 0100 0000
		return out;
	}

	private static PacketBuilder idx(Object obj, PacketBuilder out, int idx, SyncType type){
		out = init(obj, out, type);
		out.putByte(idx & 0x3f); // 0011 1111
		return out;
	}

	public static void syncEntityPropertyIds(EntityPlayer player, Entity tracked) {
		List<SyncedEntityProperties> props = ((EntityProxy) tracked)._sc_sync_getSyncedProperties();
		if (props != null) {
			new PacketEntityPropsIds(tracked, props).sendTo(player);
		}
	}

	public static List<IExtendedEntityProperties> onNewEntityProperty(Entity owner, List<IExtendedEntityProperties> syncedList, String identifier, IExtendedEntityProperties props) {
		if (Sides.logical(owner).isServer() && props instanceof SyncedEntityProperties) {
			(syncedList == null ? syncedList = Lists.newArrayList() : syncedList).add(props);
			((SyncedEntityProperties)props)._sc_sync_injectData(owner, identifier, syncedList.size() - 1);
		}
		return syncedList;
	}

	public static void tickSyncedProperties(Entity owner, List<SyncedEntityProperties> props) {
		if (Sides.logical(owner).isServer() && props != null) {
			int len = props.size();
			for (int i = 0; i < len; ++i) {
				props.get(i)._sc_sync_tick();
			}
		}
	}

	public static TypeSyncer<?> obtainSyncer(Class<?> type) {
		TypeSyncer<?> syncer = Syncing.getSyncerFor(type);
		if (syncer == null) {
			throw new RuntimeException("Couldn't determine syncer for " + type.getName());
		}
		return syncer;
	}

	public static void endSync(PacketBuilder out, PacketTarget target) {
		if (out != null) {
			out.putByte(0x80); // 1000 0000
			out.toPacket().sendTo(target);
		}
	}

	public static void endSync(PacketBuilder out, Object syncedObj, SyncType type) {
		if (out != null) {
			out.putByte(0x80); // 1000 0000

			type.sendPacket(syncedObj, out.toPacket());
		}
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, TypeSyncer<Object> syncer, Object now, Object prev) {
		if (now == null) {
			if (prev != null) { // has changed
				out = idxNull(obj, out, idx, type);
			}
		} else if (!syncer.equal(now, prev)) {
			out = idx(obj, out, idx, type);
			syncer.write(now, out);
		}
		return out;
	}

	public static int nextIdx(DataBuf in) {
		int idx = in.getByte();
		if ((idx & 0x80) == 0x80) { // 1000 0000
			return -1;
		} else {
			return idx & 0x7f; // 0111 1111
		}
	}

	public static Object read(PacketBuilder in, int idx, Object old, TypeSyncer<Object> syncer) {
		if ((idx & 0x40) == 0x40) { // 0100 0000
			return null;
		} else {
			return syncer.read(old, in);
		}
	}

	// special cases (Enum & primitives)

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, Enum<?> now, Enum<?> prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			Packets.writeEnum(out, now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, boolean now, boolean prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.putBoolean(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, byte now, byte prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.putByte(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, short now, short prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.putShort(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, int now, int prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.putInt(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, long now, long prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.putLong(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, char now, char prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.putChar(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, float now, float prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.putFloat(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, double now, double prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.putDouble(now);
		}
		return out;
	}

	public static <E extends Enum<E>> Enum<?> read_Enum(DataBuf in, Class<E> clazz) {
		return Packets.readEnum(in, clazz);
	}

	public static boolean read_boolean(DataBuf in) {
		return in.getBoolean();
	}

	public static byte read_byte(DataBuf in) {
		return in.getByte();
	}

	public static short read_short(DataBuf in) {
		return in.getShort();
	}

	public static int read_int(DataBuf in) {
		return in.getInt();
	}

	public static long read_long(DataBuf in) {
		return in.getLong();
	}

	public static char read_char(DataBuf in) {
		return in.getChar();
	}

	public static float read_float(DataBuf in) {
		return in.getFloat();
	}

	public static double read_double(DataBuf in) {
		return in.getDouble();
	}
}
