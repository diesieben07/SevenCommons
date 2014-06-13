package de.take_weiland.mods.commons.internal;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.sync.Syncing;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.List;

public final class SyncASMHooks {

	private SyncASMHooks() { }

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/SyncASMHooks";
	public static final String TICK_PROPERTIES = "tickSyncedProperties";
	public static final String WRITE_INDEX = "writeIndex";
	public static final String READ_INDEX = "readIndex";
	public static final String WRITE_INTEGRATED = "write";
	public static final String READ_PRIMITIVE = "read_%s";
	public static final String SEND_PACKET = "send_%s";
	public static final String CREATE_BUILDER = "createBuilder";
	public static final String SEND_FINISHED = "writeFinished";

	public static PacketBuilder createBuilder(Object syncInstance, SyncType type) {
		PacketBuilder b = SCModContainer.packets.builder(SCPackets.SYNC);
		DataBuffers.writeEnum(b, type);
		type.injectInfo(syncInstance, b);
		return b;
	}

	public static void writeIndex(PacketBuilder buf, int index) {
		buf.writeByte(index);
	}

	public static void writeFinished(Object syncInstance, SyncType type, PacketBuilder builder) {
		type.sendPacket(syncInstance, builder.build());
	}

	public static int readIndex(DataBuf buf) {
		return buf.readByte();
	}

	public static void write(boolean value, WritableDataBuf builder) {
		builder.writeBoolean(value);
	}

	public static void write(byte value, WritableDataBuf builder) {
		builder.writeByte(value);
	}

	public static void write(short value, WritableDataBuf builder) {
		builder.writeShort(value);
	}

	public static void write(int value, WritableDataBuf builder) {
		builder.writeInt(value);
	}

	public static void write(long value, WritableDataBuf builder) {
		builder.writeLong(value);
	}

	public static void write(char value, WritableDataBuf builder) {
		builder.writeChar(value);
	}

	public static void write(float value, WritableDataBuf builder) {
		builder.writeFloat(value);
	}

	public static void write(double value, WritableDataBuf builder) {
		builder.writeDouble(value);
	}

	public static void write(String s, WritableDataBuf builder) {
		builder.writeString(s);
	}
	public static <T> TypeSyncer<T> getSyncerFor(Class<T> toSync) {
		TypeSyncer<T> syncer = Syncing.getSyncerFor(toSync);
		if (syncer == null) {
			throw new RuntimeException("Couldn't determine syncer for " + toSync.getName());
		}
		return syncer;
	}

	private static PacketBuilder init(Object obj, PacketBuilder out, SyncType type) {
		if (out != null) {
			return out;
		}
		out = SCModContainer.packets.builder(SCPackets.SYNC);
		DataBuffers.writeEnum(out, type);
		type.injectInfo(obj, out);
		return out;
	}

	private static PacketBuilder idxNull(Object obj, PacketBuilder out, int idx, SyncType type) {
		out = init(obj, out, type);
		out.writeByte((idx & 0x3f) | 0x40); // (idx & 0011 1111) | 0100 0000
		return out;
	}

	private static PacketBuilder idx(Object obj, PacketBuilder out, int idx, SyncType type){
		out = init(obj, out, type);
		out.writeByte(idx & 0x3f); // 0011 1111
		return out;
	}

	public static void syncEntityPropertyIds(EntityPlayer player, Entity tracked) {
		List<SyncedEntityProperties> props = ((EntityProxy) tracked)._sc$getSyncedProperties();
		if (props != null) {
			new PacketEntityPropsIds(tracked, props).sendTo(player);
		}
	}

	public static List<IExtendedEntityProperties> onNewEntityProperty(Entity owner, List<IExtendedEntityProperties> syncedList, String identifier, IExtendedEntityProperties props) {
		if (Sides.logical(owner).isServer() && props instanceof SyncedEntityProperties) {
			(syncedList == null ? syncedList = Lists.newArrayList() : syncedList).add(props);
			((SyncedEntityProperties)props)._sc$injectEntityPropsData(owner, identifier);
		}
		return syncedList;
	}

	public static void tickSyncedProperties(Entity owner, List<SyncedEntityProperties> props) {
		if (Sides.logical(owner).isServer() && props != null) {
			int len = props.size();
			//noinspection ForLoopReplaceableByForEach
			for (int i = 0; i < len; ++i) {
				props.get(i)._sc$tickEntityProps();
			}
		}
	}

	public static void endSync(PacketBuilder out, PacketTarget target) {
		if (out != null) {
			out.writeByte(0x80); // 1000 0000
			out.build().sendTo(target);
		}
	}

	public static void endSync(PacketBuilder out, Object syncedObj, SyncType type) {
		if (out != null) {
			out.writeByte(0x80); // 1000 0000

			type.sendPacket(syncedObj, out.build());
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
		int idx = in.readByte();
		if ((idx & 0x80) == 0x80) { // 1000 0000
			return -1;
		} else {
			return idx & 0x7f; // 0111 1111
		}
	}

	public static Object read(DataBuf in, int idx, Object old, TypeSyncer<Object> syncer) {
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
			DataBuffers.writeEnum(out, now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, boolean now, boolean prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeBoolean(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, byte now, byte prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeByte(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, short now, short prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeShort(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, int now, int prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeInt(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, long now, long prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeLong(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, char now, char prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeChar(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, float now, float prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeFloat(now);
		}
		return out;
	}

	public static PacketBuilder sync(PacketBuilder out, Object obj, SyncType type, int idx, double now, double prev) {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeDouble(now);
		}
		return out;
	}

	public static <E extends Enum<E>> Enum<?> read_Enum(DataBuf in, Class<E> clazz) {
		return DataBuffers.readEnum(in, clazz);
	}

	public static boolean read_boolean(DataBuf in) {
		return in.readBoolean();
	}

	public static byte read_byte(DataBuf in) {
		return in.readByte();
	}

	public static short read_short(DataBuf in) {
		return in.readShort();
	}

	public static int read_int(DataBuf in) {
		return in.readInt();
	}

	public static long read_long(DataBuf in) {
		return in.readLong();
	}

	public static char read_char(DataBuf in) {
		return in.readChar();
	}

	public static float read_float(DataBuf in) {
		return in.readFloat();
	}

	public static double read_double(DataBuf in) {
		return in.readDouble();
	}
}
