package de.take_weiland.mods.commons.asm;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.IExtendedEntityProperties;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import de.take_weiland.mods.commons.fastreflect.Fastreflect;
import de.take_weiland.mods.commons.internal.CommonsModContainer;
import de.take_weiland.mods.commons.internal.CommonsPackets;
import de.take_weiland.mods.commons.internal.EntityProxy;
import de.take_weiland.mods.commons.net.Packets;
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
	
	@SuppressWarnings("unchecked")
	public static Function<?, Packet> makeSyncGroupInvoker(Class<?> toSync, String targetName) throws Exception {
		String targetDesc = getMethodDescriptor(getType(Packet.class));
		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;
		
		String name = Fastreflect.nextDynamicClassName();
		
		cw.visit(V1_6, ACC_PUBLIC, name, null, "java/lang/Object", new String[] { "com/google/common/base/Function" });
		cw.visitSource(".dynamic", null);
		
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", getMethodDescriptor(VOID_TYPE), null, null);
		mv.visitCode();
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", getMethodDescriptor(VOID_TYPE));
		mv.visitInsn(RETURN);
		
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
		Type targetType = getType(toSync);
		mv = cw.visitMethod(ACC_PUBLIC, "apply", getMethodDescriptor(getType(Object.class), getType(Object.class)), null, null);
		mv.visitCode();
		
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(CHECKCAST, targetType.getInternalName());
		mv.visitMethodInsn(INVOKEVIRTUAL, targetType.getInternalName(), targetName, targetDesc);
		
		mv.visitInsn(ARETURN);
		
		mv.visitMaxs(1, 3);
		
		mv.visitEnd();
		
		cw.visitEnd();
		return Fastreflect.defineDynamicClass(cw.toByteArray()).asSubclass(Function.class).newInstance();
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
	
	public static Packet endSyncMakePacket(ByteArrayDataOutput out, Object syncedObj, SyncType type) {
		if (out != null) {
			out.writeByte(0x80); // 1000 0000
			return CommonsModContainer.packetTransport.make(out.toByteArray(), CommonsPackets.SYNC);
		} else {
			return null;
		}
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
	
	public static Object read(DataInput in, int idx, Object old, TypeSyncer<Object> syncer) throws IOException {
		if ((idx & 0x40) == 0x40) { // 0100 0000
			return null;
		} else {
			return syncer.read(old, in);
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
	
	public static <E extends Enum<E>> Enum<?> read_Enum(DataInput in, Class<E> clazz) throws IOException {
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
