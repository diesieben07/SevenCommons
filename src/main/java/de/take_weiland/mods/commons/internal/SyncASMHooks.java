package de.take_weiland.mods.commons.internal;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.List;
import java.util.UUID;

public final class SyncASMHooks {

	private SyncASMHooks() { }

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/SyncASMHooks";
	public static final String TICK_PROPERTIES = "tickSyncedProperties";
	public static final String WRITE_INDEX = "writeIndex";
	public static final String READ_INDEX = "readIndex";
	public static final String WRITE_INTEGRATED = "write";
	public static final String READ_INTEGRATED = "read_%s";
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

	public static void write(UUID uuid, WritableDataBuf buf) {
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
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

	public static <E extends Enum<E>> Enum<?> read_Enum(DataBuf in, Class<E> clazz) {
		return DataBuffers.readEnum(in, clazz);
	}

	public static UUID read_java_util_UUID(DataBuf in) {
		return new UUID(in.readLong(), in.readLong());
	}

	public static String read_java_lang_String(DataBuf in) {
		return in.readString();
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
