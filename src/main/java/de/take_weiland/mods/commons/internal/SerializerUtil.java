package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.util.ByteStreamSerializable;

import java.lang.annotation.Annotation;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public class SerializerUtil {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/SerializerUtil";
	public static final String BOOTSTRAP = "inDyBootstrap";
	public static final String BYTESTREAM = "bytestream";

	public static CallSite inDyBootstrap(MethodHandles.Lookup lookup, String name, MethodType callType) {
		if (name.equals(BYTESTREAM)) {
			if (callType.parameterCount() != 2) {
				throw err("Parameter count mismatch");
			}
			if (callType.parameterType(0) != Class.class || callType.parameterType(1) != MCDataInputStream.class) {
				throw err("Parameter type mismatch");
			}
			if (!ByteStreamSerializable.class.isAssignableFrom(callType.returnType())) {
				throw err("Return type mismatch");
			}
			// get a MethodHandle that takes a MethodHandle and the InputStream and invokes the handle with the stream
			MethodHandle invoker = MethodHandles.invoker(methodType(callType.returnType(), MCDataInputStream.class)); // MethodHandle<MethodHandle, MCDataInputStream>

			// get a MethodHandle that transforms the class into a MethodHandle via getDeserializer
			// and then invokes the above MethodHandle with that MethodHandle as the argument
			MethodHandle filtered = MethodHandles.filterArguments(invoker, 0, getDeserializer); // MethodHandle<Class, MCDataInputStream>

			return new ConstantCallSite(filtered);
		} else {
			throw new RuntimeException("Invalid call to SerializerUtil.inDyBootstrap!");
		}
	}

	private static RuntimeException err(String desc) {
		return new RuntimeException("Invalid InvokeDynamic call: " + desc);
	}

	private static final MethodHandle getDeserializer;
	private static final Map<Class<?>, MethodHandle> deserializers = new ConcurrentHashMap<>();
	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	static {
		try {
			getDeserializer = lookup.findStatic(SerializerUtil.class, "getDeserializer", methodType(MethodHandle.class, Class.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	public static MethodHandle getDeserializer(Class<?> clazz) {
		MethodHandle mh = deserializers.get(clazz);
		if (mh == null) {
			return lookupDeserializer(clazz);
		}
		return mh;
	}

	public static MethodHandle lookupDeserializer(Class<?> clazz) {
		Method method = findDeserializer(clazz, ByteStreamSerializable.Deserializer.class, MCDataInputStream.class);
		try {
			MethodHandle mh = lookup.unreflect(method);
			deserializers.put(clazz, mh);
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
			method.setAccessible(true);

			return method;
		}
		throw new IllegalStateException("Missing @Deserializer method in " + clazz.getName());
	}



}
