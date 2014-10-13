package de.take_weiland.mods.commons.util;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.CannotSerializeException;
import de.take_weiland.mods.commons.internal.SerializerUtil;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>Registry for {@link de.take_weiland.mods.commons.util.ByteStreamSerializer}.</p>
 * <p>Classes supported by default are:</p>
 * <ul>
 *     <li>Implementers of {@code ByteStreamSerializable}</li>
 *     <li>Strings</li>
 *     <li>ItemStacks</li>
 *     <li>FluidStacks</li>
 *     <li>UUIDs</li>
 * </ul>
 * @see de.take_weiland.mods.commons.net.MCDataOutput
 * @see de.take_weiland.mods.commons.net.MCDataInput
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class ByteStreamSerializers {

	private static final ConcurrentMap<Class<?>, ByteStreamSerializer<?>> serializers = new MapMaker().concurrencyLevel(2).makeMap();

	/**
	 * <p>Get a {@code ByteStreamSerializer} that can serialize instances of the given Class.</p>
	 * @param clazz the class to be serialized
	 * @return a ByteStreamSerializer
	 * @throws de.take_weiland.mods.commons.CannotSerializeException if the given class cannot be serialized
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public static <T> ByteStreamSerializer<T> getSerializer(Class<T> clazz) {
		ByteStreamSerializer<T> serializer = (ByteStreamSerializer<T>) serializers.get(clazz);
		if (serializer == null) {
			if (serializers.putIfAbsent(clazz, (serializer = newSerializer(clazz))) != null) {
				return (ByteStreamSerializer<T>) serializers.get(clazz);
			}
		}
		return serializer;
	}

	/**
	 * <p>Register a class as being serializable with the given serializer.</p>
	 * @param clazz the Class
	 * @param serializer the Serializer
	 * @throws java.lang.IllegalArgumentException if there is a Serializer already registered for the given class
	 */
	public static <T> void registerSerializer(Class<T> clazz, ByteStreamSerializer<T> serializer) {
		checkArgument(!ByteStreamSerializable.class.isAssignableFrom(clazz), "Don't register ByteStreamSerializable wrappers manually");
		if (serializers.putIfAbsent(clazz, serializer) != null) {
			throw new IllegalArgumentException("Serializer for " + clazz.getName() + " already registered");
		}
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
			throw new CannotSerializeException(clazz);
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
				return SerializerUtil.readByteStreamViaDeserializer(clazz, in);
			}
		};
	}

	private ByteStreamSerializers() { }

}
