package de.take_weiland.mods.commons.util;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.internal.SerializerUtil;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

/**
 * <p>Utilities for working with {@link de.take_weiland.mods.commons.util.ByteStreamSerializer} and
 * {@link de.take_weiland.mods.commons.util.ByteStreamSerializable}.</p>
 *
 * @author diesieben07
 */
public final class Serializers {

	/**
	 * <p>Wrap the given ByteStreamSerializable class into a ByteStreamSerializer. The generated serializer supports
	 * null values. If that is not needed, {@link #wrap(Class, boolean)} should be used instead.</p>
	 * @param clazz the class
	 * @return a ByteStreamSerializer
	 */
	public static <T extends ByteStreamSerializable> ByteStreamSerializer<T> wrap(Class<T> clazz) {
		return wrap(clazz, true);
	}

	private static Map<Class<?>, ByteStreamSerializer<?>> compiledSerializersNullable;
	private static Map<Class<?>, ByteStreamSerializer<?>> compiledSerializersNonNull;

	/**
	 * <p>Wrap the given ByteStreamSerializable class into a ByteStreamSerializer.</p>
	 * <p>If {@code nullable} is false, the returned serializer will throw a {@code NullPointerException}
	 * when a null value is passed in and it will never return a null value.</p>
	 * @param clazz the class
	 * @param nullable if the generated class should support null values
	 * @return a ByteStreamSerializer
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ByteStreamSerializable> ByteStreamSerializer<T> wrap(Class<T> clazz, boolean nullable) {
		Map<Class<?>, ByteStreamSerializer<?>> map;
		if (nullable) {
			if (compiledSerializersNullable == null) {
				compiledSerializersNullable = Maps.newHashMap();
			}
			map = compiledSerializersNonNull;
		} else {
			if (compiledSerializersNonNull == null) {
				compiledSerializersNonNull = Maps.newHashMap();
			}
			map = compiledSerializersNonNull;
		}
		ByteStreamSerializer<T> serializer = (ByteStreamSerializer<T>) map.get(clazz);
		if (serializer == null) {
			map.put(clazz, (serializer = compileSerializer(clazz, nullable)));
		}
		return serializer;
	}

	private static ByteStreamSerializer<ItemStack> itemStack;

	/**
	 * <p>Get a ByteStreamSerializer for serializing ItemStacks. The serializer will support null values.</p>
	 * @return a ByteStreamSerializer
	 */
	public static ByteStreamSerializer<ItemStack> forItemStack() {
		if (itemStack == null) {
			itemStack = new ByteStreamSerializer<ItemStack>() {
				@Override
				public void write(ItemStack instance, MCDataOutputStream out) {
					out.writeItemStack(instance);
				}

				@Override
				public ItemStack read(MCDataInputStream in) {
					return in.readItemStack();
				}
			};
		}
		return itemStack;
	}

	private static ByteStreamSerializer<FluidStack> fluidStack;

	/**
	 * <p>Get a ByteStreamSerializer for serializing FluidStacks. The serializer will support null values.</p>
	 * @return a ByteStreamSerializer
	 */
	public static ByteStreamSerializer<FluidStack> forFluidStack() {
		if (fluidStack == null) {
			fluidStack = new ByteStreamSerializer<FluidStack>() {
				@Override
				public void write(FluidStack instance, MCDataOutputStream out) {
					out.writeFluidStack(instance);
				}

				@Override
				public FluidStack read(MCDataInputStream in) {
					return in.readFluidStack();
				}
			};
		}
		return fluidStack;
	}

	private static ByteStreamSerializer<UUID> uuid;

	/**
	 * <p>Get a ByteStreamSerializer for serializing UUIDs. The serializer will support null values.</p>
	 * @return a ByteStreamSerializer
	 */
	public static ByteStreamSerializer<UUID> forUUID() {
		if (uuid == null) {
			uuid = new ByteStreamSerializer<UUID>() {
				@Override
				public void write(UUID instance, MCDataOutputStream out) {
					out.writeUUID(instance);
				}

				@Override
				public UUID read(MCDataInputStream in) {
					return in.readUUID();
				}
			};
		}
		return uuid;
	}

	private static ByteStreamSerializer<String> string;

	/**
	 * <p>Get a ByteStreamSerializer for serializing Strings. The serializer will support null values.</p>
	 * @return a ByteStreamSerializer
	 */
	public static ByteStreamSerializer<String> forString() {
		if (string == null) {
			string = new ByteStreamSerializer<String>() {

				@Override
				public void write(String instance, MCDataOutputStream out) {
					out.writeString(instance);
				}

				@Override
				public String read(MCDataInputStream in) {
					return in.readString();
				}
			};
		}
		return string;
	}

	private static <T extends ByteStreamSerializable> ByteStreamSerializer<T> compileSerializer(Class<T> clazz, boolean nullable) {
		Method method = SerializerUtil.findDeserializer(clazz, ByteStreamSerializable.Deserializer.class, MCDataInputStream.class);
		return nullable ? new SerializerNullable<T>(method) : new SerializerNonNull<T>(method);
	}


	private static class SerializerNullable<T extends ByteStreamSerializable> implements ByteStreamSerializer<T> {

		private final Method method;

		private SerializerNullable(Method method) {
			this.method = method;
		}

		@Override
		public void write(T instance, MCDataOutputStream out) {
			if (instance == null) {
				out.writeBoolean(true);
			} else {
				out.writeBoolean(false);
				instance.writeTo(out);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public T read(MCDataInputStream in) {
			if (in.readBoolean()) {
				return null;
			} else {
				try {
					return (T) method.invoke(null, in);
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}
			}
		}
	}

	private static class SerializerNonNull<T extends ByteStreamSerializable> implements ByteStreamSerializer<T> {
		private final Method method;

		public SerializerNonNull(Method method) {
			this.method = method;
		}

		@Override
		public void write(T instance, MCDataOutputStream out) {
			instance.writeTo(out);
		}

		@SuppressWarnings("unchecked")
		@Override
		public T read(MCDataInputStream in) {
			try {
				return (T) method.invoke(null, in);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}
	}

	private Serializers() { }

}
