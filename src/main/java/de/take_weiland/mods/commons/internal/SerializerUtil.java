package de.take_weiland.mods.commons.internal;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.nbt.NBTSerializable;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import net.minecraft.nbt.NBTBase;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentMap;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class SerializerUtil {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/SerializerUtil";
	public static final String BOOTSTRAP = "inDyBootstrap";
	public static final String BYTESTREAM = "bytestream";
	public static final String NBT = "nbt";

	private static final MethodHandle getBSDeserializer;
	private static final MethodHandle getNBTDeserializer;
	private static final ConcurrentMap<Class<?>, MethodHandle> bsDeserializers;
	private static final ConcurrentMap<Class<?>, MethodHandle> nbtDeserializers;

	static {
		MapMaker mm = new MapMaker().concurrencyLevel(2);
		bsDeserializers = mm.makeMap();
		nbtDeserializers = mm.makeMap();

		MethodHandles.Lookup lookup = MethodHandles.lookup();
		try {
			getBSDeserializer = lookup.findStatic(SerializerUtil.class, "getBSDeserializer", methodType(MethodHandle.class, Class.class));
			getNBTDeserializer = lookup.findStatic(SerializerUtil.class, "getNBTDeserializer", methodType(MethodHandle.class, Class.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * <p>This method is the bootstrap method for the InvokeDynamic call from {@link de.take_weiland.mods.commons.util.ByteStreamSerializers#read(Class, de.take_weiland.mods.commons.net.MCDataInputStream)}.</p>
	 * <p>It produces a ConstantCallSite, which is permanently linked to call a MethodHandle looked up via getBSDeserializer.</p>
	 * <p>The MethodType must follow the following form:</p>
	 * <ul>
	 *     <li>For "bytestream": returntype: ByteStreamSerializable, params: Class, MCDataInputStream</li>
	 *     <li>For "nbt": returntype: NBTSerializable, params: Class, NBTBase</li>
	 * </ul>
	 * @param lookup lookup
	 * @param name type of Deserializer, can be either "bytestream" or "nbt" at this point in time.
	 * @param callType the expected MethodHandle type
	 * @return CallSite
	 */
	public static CallSite inDyBootstrap(MethodHandles.Lookup lookup, String name, MethodType callType) {
		switch (name) {
			case BYTESTREAM: {
				if (callType.parameterCount() != 2) {
					throw err("Parameter count mismatch");
				}
				if (callType.parameterType(0) != Class.class || callType.parameterType(1) != MCDataInputStream.class) {
					throw err("Parameter type mismatch");
				}
				if (callType.returnType() != ByteStreamSerializable.class) {
					throw err("Return type mismatch");
				}
				// get a MethodHandle that takes a MethodHandle and the InputStream and invokes the handle with the stream
				MethodHandle invoker = MethodHandles.invoker(methodType(ByteStreamSerializable.class, MCDataInputStream.class)); // MethodHandle<MethodHandle, MCDataInputStream>


				// get a MethodHandle that transforms the class into a MethodHandle via getBSDeserializer
				// and then invokes the above MethodHandle with that MethodHandle as the argument
				MethodHandle filtered = MethodHandles.filterArguments(invoker, 0, getBSDeserializer); // MethodHandle<Class, MCDataInputStream>


				return new ConstantCallSite(filtered);
			}
			case NBT: {
				if (callType.parameterCount() != 2) {
					throw err("Parameter count mismatch");
				}
				if (callType.parameterType(0) != Class.class || callType.parameterType(1) != NBTBase.class) {
					throw err("Parameter type mismatch");
				}
				if (callType.returnType() != NBTSerializable.class) {
					throw err("Return type mismatch");
				}

				// similar to above
				MethodHandle invoker = MethodHandles.invoker(methodType(NBTSerializable.class, NBTBase.class));
				MethodHandle filtered = MethodHandles.filterArguments(invoker, 0, getNBTDeserializer);
				return new ConstantCallSite(filtered);
			}
			default:
				throw err("Invalid name!");
		}
	}

	private static RuntimeException err(String desc) {
		return new RuntimeException("Invalid InvokeDynamic call: " + desc);
	}

	private static MethodHandle getBSDeserializer(Class<?> clazz) {
		MethodHandle mh = bsDeserializers.get(clazz);
		if (mh == null) {
			return lookupBSDeserializer(clazz);
		}
		return mh;
	}

	private static MethodHandle lookupBSDeserializer(Class<?> clazz) {
		Method method = findDeserializer(clazz, ByteStreamSerializable.Deserializer.class, MCDataInputStream.class);
		try {
			MethodHandle mh = MethodHandles.publicLookup().unreflect(method);
			if (bsDeserializers.putIfAbsent(clazz, mh) != null) {
				return bsDeserializers.get(clazz);
			}
			return mh;
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access Deserializer in " + clazz.getName(), e);
		}
	}

	private static MethodHandle getNBTDeserializer(Class<?> clazz) {
		MethodHandle mh = nbtDeserializers.get(clazz);
		if (mh == null) {
			return lookupNBTDeserializer(clazz);
		}
		return mh;
	}

	private static MethodHandle lookupNBTDeserializer(Class<?> clazz) {
		Method method = findDeserializer(clazz, NBTSerializable.Deserializer.class, NBTBase.class);
		try {
			MethodHandle mh = MethodHandles.publicLookup().unreflect(method);
			if (nbtDeserializers.putIfAbsent(clazz, mh) != null) {
				return nbtDeserializers.get(clazz);
			}
			return mh;
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access Deserializer in " + clazz.getName(), e);
		}
	}

	private static Method findDeserializer(Class<?> clazz, Class<? extends Annotation> annotation, Class<?> paramType) {
		for (final Method method : clazz.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(annotation)) {
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
			if (!params[0].isAssignableFrom(paramType)) {
				throw new IllegalStateException("@Deserializer has invalid parameter type in " + clazz.getName());
			}
			return method;
		}
		throw new IllegalStateException("Missing @Deserializer method in " + clazz.getName());
	}


	@InvokeDynamic(name = BYTESTREAM, bootstrapClass = CLASS_NAME, bootstrapMethod = BOOTSTRAP)
	public static <T extends ByteStreamSerializable> T readByteStreamViaDeserializer(Class<T> clazz, MCDataInputStream in) {
		throw new AssertionError("InvokeDynamic failed");
	}

	@InvokeDynamic(name = NBT, bootstrapClass = CLASS_NAME, bootstrapMethod = BOOTSTRAP)
	public static <T extends NBTSerializable> T readNBTViaDeserializer(Class<T> clazz, NBTBase nbt) {
		throw new AssertionError("InvokeDynamic failed");
	}

	private SerializerUtil() { }
}
