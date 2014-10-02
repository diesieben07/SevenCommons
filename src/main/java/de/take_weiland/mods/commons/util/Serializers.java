package de.take_weiland.mods.commons.util;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.internal.InvokeDynamic;
import de.take_weiland.mods.commons.internal.SerializerUtil;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>Utilities for working with {@link de.take_weiland.mods.commons.util.ByteStreamSerializer} and
 * {@link de.take_weiland.mods.commons.util.ByteStreamSerializable}.</p>
 *
 * @author diesieben07
 */
public final class Serializers {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/util/Serializers";
	public static final String DESERIALIZE = "deserialize";


	public static <T> T read(Class<T> clazz, MCDataInputStream in) {
		return getSerializer(clazz).read(in);
	}

	@InvokeDynamic(name = SerializerUtil.BYTESTREAM, bootstrapClass = SerializerUtil.CLASS_NAME, bootstrapMethod = SerializerUtil.BOOTSTRAP)
	private static <T extends ByteStreamSerializable> T readViaStaticMethod(Class<T> clazz, MCDataInputStream in) {
		throw new AssertionError("InvokeDynamic failed");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> void write(T instance, MCDataOutputStream out) {
		((ByteStreamSerializer) getSerializer(instance.getClass())).write(instance, out);
	}

	private static final ConcurrentMap<Class<?>, ByteStreamSerializer<?>> serializers = new MapMaker().concurrencyLevel(2).makeMap();

	@SuppressWarnings("unchecked")
	public static <T> ByteStreamSerializer<T> getSerializer(@NotNull Class<T> clazz) {
		ByteStreamSerializer<T> serializer = (ByteStreamSerializer<T>) serializers.get(clazz);
		if (serializer == null) {
			if (serializers.putIfAbsent(clazz, (serializer = newSerializer(clazz))) != null) {
				return (ByteStreamSerializer<T>) serializers.get(clazz);
			}
		}
		return serializer;
	}

	@SuppressWarnings("unchecked")
	@NotNull
	public static <T extends ByteStreamSerializable> ByteStreamSerializer<T> wrap(@NotNull Class<T> clazz) {
		ByteStreamSerializer<T> wrapper = (ByteStreamSerializer<T>) serializers.get(clazz);
		if (wrapper == null) {
			if (serializers.putIfAbsent(clazz, (wrapper = newWrapper(clazz))) != null) {
				// someone got there first
				return (ByteStreamSerializer<T>) serializers.get(clazz);
			}
		}
		return wrapper;
	}

	@SuppressWarnings("unchecked")
	private static <T> ByteStreamSerializer<T> newSerializer(Class<T> clazz) {
		if (clazz == ItemStack.class) {
			return (ByteStreamSerializer<T>) new ByteStreamSerializer<ItemStack>() {
				@Override
				public void write(ItemStack instance, MCDataOutputStream out) {
					out.writeItemStack(instance);
				}

				@Override
				public ItemStack read(MCDataInputStream in) {
					return in.readItemStack();
				}
			};
		} else if (clazz == FluidStack.class) {
			return (ByteStreamSerializer<T>) new ByteStreamSerializer<FluidStack>() {

				@Override
				public void write(FluidStack instance, MCDataOutputStream out) {
					out.writeFluidStack(instance);
				}

				@Override
				public FluidStack read(MCDataInputStream in) {
					return in.readFluidStack();
				}
			};
		} else if (clazz == String.class) {
			return (ByteStreamSerializer<T>) new ByteStreamSerializer<String>() {

				@Override
				public void write(String instance, MCDataOutputStream out) {
					out.writeString(instance);
				}

				@Override
				public String read(MCDataInputStream in) {
					return in.readString();
				}
			};
		} else if (clazz == UUID.class) {
			return (ByteStreamSerializer<T>) new ByteStreamSerializer<UUID>() {

				@Override
				public void write(UUID instance, MCDataOutputStream out) {
					out.writeUUID(instance);
				}

				@Override
				public UUID read(MCDataInputStream in) {
					return in.readUUID();
				}
			};
		} else if (ByteStreamSerializable.class.isAssignableFrom(clazz)) {
			return (ByteStreamSerializer<T>) newWrapper((Class<ByteStreamSerializable>) clazz);
		} else {
			throw new RuntimeException("Cannot serialize " + clazz.getName());
		}
	}

	private static <T extends ByteStreamSerializable> ByteStreamSerializer<T> newWrapper(final Class<T> clazz) {
		return new ByteStreamSerializer<T>() {
			@Override
			public void write(T instance, MCDataOutputStream out) {
				instance.writeTo(out);
			}

			@Override
			public T read(MCDataInputStream in) {
				return Serializers.readViaStaticMethod(clazz, in);
			}
		};
	}

	private Serializers() { }

}
