package de.take_weiland.mods.commons.util;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.UUID;

/**
 * @author diesieben07
 */
public final class Serializers {

	private static Map<Class<?>, ByteStreamSerializer<?>> customSerializers;

	@SuppressWarnings("unchecked")
	public static <T extends ByteStreamSerializable> ByteStreamSerializer<T> wrap(Class<T> clazz) {
		if (customSerializers == null) {
			customSerializers = Maps.newHashMap();
		}
		ByteStreamSerializer<T> serializer = (ByteStreamSerializer<T>) customSerializers.get(clazz);
		if (serializer == null) {
			customSerializers.put(clazz, (serializer = compileSerializer(clazz)));
		}
		return serializer;
	}

	private static <T extends ByteStreamSerializable> ByteStreamSerializer<T> compileSerializer(Class<T> clazz) {
		for (final Method method : clazz.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(ByteStreamSerializable.Deserializer.class)) {
				continue;
			}
			if (!Modifier.isStatic(method.getModifiers())) {
				throw new IllegalStateException("@Deserializer on non-static method in " + clazz.getName());
			}
			Class<?> returnType = method.getReturnType();
			if (!clazz.isAssignableFrom(returnType)) {
				throw new IllegalStateException("@Deserializer returns wrong type in " + clazz.getName());
			}

			Class<?>[] params = method.getParameterTypes();
			if (params.length != 1) {
				throw new IllegalStateException("@Deserializer takes more than one parameter in " + clazz.getName());
			}
			if (!params[0].isAssignableFrom(MCDataInputStream.class)) {
				throw new IllegalStateException("@Deserializer has invalid parameter type in " + clazz.getName());
			}
			method.setAccessible(true);

			return new ByteStreamSerializer<T>() {
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
			};
		}
		throw new IllegalStateException("No @Deserializer method found in " + clazz.getName());
	}

	private static ByteStreamSerializer<ItemStack> itemStack;

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

	private Serializers() { }

}
