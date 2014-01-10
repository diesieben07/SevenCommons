package de.take_weiland.mods.commons.internal.sync;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.sun.istack.internal.logging.Logger;

import de.take_weiland.mods.commons.asm.transformers.SyncingTransformer;
import de.take_weiland.mods.commons.network.Packets;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.util.Fluids;
import de.take_weiland.mods.commons.util.ItemStacks;

public class Syncer {

	private static final Map<Class<?>, TypeSyncer<?>> customSyncers = Maps.newHashMap();
	
	public static <T> void registerSyncer(Class<T> clazz, TypeSyncer<? super T> syncer) {
		customSyncers.put(clazz, syncer);
	}
	
	static {
		registerDefaultSyncers();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void registerDefaultSyncers() {
		registerSyncer(String.class, new TypeSyncer<String>() {

			@Override
			public boolean equal(String a, String b) {
				return Objects.equal(a, b);
			}

			@Override
			public void write(String s, DataOutput out) throws IOException {
				out.writeUTF(Strings.nullToEmpty(s));
			}

			@Override
			public String read(Class<? extends String> clazz, DataInput in) throws IOException {
				return in.readUTF();
			}
		});
		
		registerSyncer(FluidStack.class, new TypeSyncer<FluidStack>() {

			@Override
			public boolean equal(FluidStack a, FluidStack b) {
				return Fluids.identical(a, b);
			}

			@Override
			public void write(FluidStack fluid, DataOutput out) throws IOException {
				Packets.writeFluidStack(out, fluid);
			}

			@Override
			public FluidStack read(Class<? extends FluidStack> clazz, DataInput in) throws IOException {
				return Packets.readFluidStack(in);
			}
			
		});
		
		registerSyncer(ItemStack.class, new TypeSyncer<ItemStack>() {

			@Override
			public boolean equal(ItemStack a, ItemStack b) {
				return ItemStacks.equal(a, b);
			}

			@Override
			public void write(ItemStack stack, DataOutput out) throws IOException {
				Packet.writeItemStack(stack, out);
			}

			@Override
			public ItemStack read(Class<? extends ItemStack> clazz, DataInput in) throws IOException {
				return Packet.readItemStack(in);
			}
		});
		
		registerSyncer(Enum.class, new TypeSyncer<Enum>() {

			@Override
			public boolean equal(Enum a, Enum b) {
				return a == b;
			}

			@Override
			public void write(Enum e, DataOutput out) throws IOException {
				Packets.writeEnum(out, e);
			}

			@Override
			public Enum read(Class<? extends Enum> clazz, DataInput in) throws IOException {
				return Packets.readEnum(in, clazz);
			}
		});
	}
	
	@SuppressWarnings("unchecked") // safe casts, because the map only gets filled from registerSyncer
	private static <T> TypeSyncer<T> syncerFor(Class<T> oClass) {
		TypeSyncer<?> syncer = customSyncers.get(oClass);
		if (syncer != null) {
			return (TypeSyncer<T>) syncer;
		}
		Class<?> clazz = oClass;
		do {
			clazz = clazz.getSuperclass();
			Preconditions.checkArgument(!clazz.equals(Object.class), "No TypeSyncer for %s", oClass.getSimpleName());
			syncer = customSyncers.get(clazz);
		} while (syncer == null);
		customSyncers.put(oClass, syncer); // make it faster next time
		return (TypeSyncer<T>) syncer;
	}
	
	public static void performSync(SyncedObject obj, SyncType type) {
		if (obj._SC_SYNC_isDirty()) {
			Packet packet = new PacketSync(obj, type).make();
			switch (type) {
			case CONTAINER:
				Packets.sendPacketToViewing(packet, (Container) obj);
				break;
			case TILE_ENTITY:
				Packets.sendPacketToAllTracking(packet, (TileEntity)obj);
				break;
			case ENTITY:
				Packets.sendPacketToAllTracking(packet, (Entity)obj);
				break;
			}
		}
	}
	
	public static void throwErr() {
		SyncingTransformer.LOGGER.warning("Invalid field index!");
	}
	
	public static void writeIdx(int idx, DataOutput out) throws IOException {
		out.writeByte(idx);
	}
	
	public static int readIdx(DataInput in) throws IOException {
		return in.readByte();
	}

	public static <T> boolean equal(T a, T b, Class<T> clazz) {
		return syncerFor(clazz).equal(a, b);
	}
	
	public static <T> void write(T a, Class<T> clazz, DataOutput out) throws IOException {
		syncerFor(clazz).write(a, out);
	}
	
	public static <T> T read(DataInput in, Class<T> clazz) throws IOException {
		return syncerFor(clazz).read(clazz, in);
	}
	
	public static boolean equal(boolean a, boolean b) {
		return a == b;
	}
	
	public static boolean equal(byte a, byte b) {
		return a == b;
	}
	
	public static boolean equal(short a, short b) {
		return a == b;
	}
	
	public static boolean equal(int a, int b) {
		return a == b;
	}
	
	public static boolean equal(long a, long b) {
		return a == b;
	}
	
	public static boolean equal(float a, float b) {
		return a == b;
	}
	
	public static boolean equal(double a, double b) {
		return a == b;
	}
	
	public static boolean equal(char a, char b) {
		return a == b;
	}
	
	public static void write(boolean a, DataOutput out) throws IOException {
		out.writeBoolean(a);
	}
	
	public static void write(byte a, DataOutput out) throws IOException {
		out.writeByte(a);
	}
	
	public static void write(short a, DataOutput out) throws IOException {
		out.writeShort(a);
	}
	
	public static void write(int a, DataOutput out) throws IOException {
		out.writeInt(a);
	}
	
	public static void write(long a, DataOutput out) throws IOException {
		out.writeLong(a);
	}
	
	public static void write(float a, DataOutput out) throws IOException {
		out.writeFloat(a);
	}
	
	public static void write(double a, DataOutput out) throws IOException {
		out.writeDouble(a);
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
	
	public static float read_float(DataInput in) throws IOException {
		return in.readFloat();
	}
	
	public static double read_double(DataInput in) throws IOException {
		return in.readDouble();
	}
	
}
