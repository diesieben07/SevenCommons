package de.take_weiland.mods.commons.asm;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.IExtendedEntityProperties;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import de.take_weiland.mods.commons.internal.CommonsModContainer;
import de.take_weiland.mods.commons.internal.CommonsPackets;
import de.take_weiland.mods.commons.internal.EntityProxy;
import de.take_weiland.mods.commons.network.Packets;
import de.take_weiland.mods.commons.sync.PacketEntityPropsIds;
import de.take_weiland.mods.commons.sync.SyncType;
import de.take_weiland.mods.commons.sync.SyncedEntityProperties;
import de.take_weiland.mods.commons.sync.Syncing;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.util.Sides;

public final class SyncASMHooks {

	private SyncASMHooks() { }
	
	private static ByteArrayDataOutput init(Object obj, ByteArrayDataOutput out, SyncType type) throws IOException {
		if (out != null) {
			return out;
		}
		out = ByteStreams.newDataOutput();
		CommonsModContainer.packetTransport.prepareOutput(out, CommonsPackets.SYNC);
		Packets.writeEnum(out, type);
		type.injectInfo(obj, out);
		return out;
	}
	
	private static ByteArrayDataOutput idxNull(Object obj, ByteArrayDataOutput out, int idx, SyncType type) throws IOException {
		out = init(obj, out, type);
		out.writeByte((idx & 0x3f) | 0x40); // (idx & 0011 1111) | 0100 0000
		return out;
	}
	
	private static ByteArrayDataOutput idx(Object obj, ByteArrayDataOutput out, int idx, SyncType type) throws IOException {
		out = init(obj, out, type);
		out.writeByte(idx & 0x3f); // 0011 1111
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
			System.out.println("new property: " + identifier);
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
	
	public static void endSync(ByteArrayDataOutput out, Object syncedObj, SyncType type) {
		if (out != null) {
			out.writeByte(0x80); // 1000 0000
			Packet p = CommonsModContainer.packetTransport.make(out.toByteArray(), CommonsPackets.SYNC);
			type.sendPacket(syncedObj, p);
		}
	}
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, TypeSyncer<Object> syncer, Object now, Object prev) throws IOException {
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
	
	public static int nextIdx(DataInput in) throws IOException {
		int idx = in.readUnsignedByte();
		if ((idx & 0x80) == 0x80) { // 1000 0000
			return -1;
		} else {
			return idx & 0x7f; // 0111 1111
		}
	}
	
	public static Object read(DataInput in, int idx, TypeSyncer<Object> syncer) throws IOException {
		if ((idx & 0x40) == 0x40) { // 0100 0000
			return null;
		} else {
			return syncer.read(in);
		}
	}
	
	// special cases (Enum & primitives)
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, Enum<?> now, Enum<?> prev) throws IOException {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			Packets.writeEnum(out, now);
		}
		return out;
	}
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, boolean now, boolean prev) throws IOException {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeBoolean(now);
		}
		return out;
	}
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, byte now, byte prev) throws IOException {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeByte(now);
		}
		return out;
	}
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, short now, short prev) throws IOException {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeShort(now);
		}
		return out;
	}
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, int now, int prev) throws IOException {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeInt(now);
		}
		return out;
	}
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, long now, long prev) throws IOException {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeLong(now);
		}
		return out;
	}
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, char now, char prev) throws IOException {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeChar(now);
		}
		return out;
	}
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, float now, float prev) throws IOException {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeFloat(now);
		}
		return out;
	}
	
	public static ByteArrayDataOutput sync(ByteArrayDataOutput out, Object obj, SyncType type, int idx, double now, double prev) throws IOException {
		if (now != prev) {
			out = idx(obj, out, idx, type);
			out.writeDouble(now);
		}
		return out;
	}
	
	public static <E extends Enum<E>>Enum<?> read_Enum(DataInput in, Class<E> clazz) throws IOException {
		return Packets.readEnum(in, clazz);
	}
	
	public static boolean read_boolean(DataInput in) throws IOException {
		return in.readBoolean();
	}
	
	public static byte read_byte(DataInput in) throws IOException {
		return in.readByte();
	}
	
	public static short read_short(DataInput in) throws IOException {
		return in.readShort();
	}
	
	public static int read_int(DataInput in) throws IOException {
		return in.readInt();
	}
	
	public static long read_long(DataInput in) throws IOException {
		return in.readLong();
	}
	
	public static char read_char(DataInput in) throws IOException {
		return in.readChar();
	}
	
	public static float read_float(DataInput in) throws IOException {
		return in.readFloat();
	}
	
	public static double read_double(DataInput in) throws IOException {
		return in.readDouble();
	}
}
