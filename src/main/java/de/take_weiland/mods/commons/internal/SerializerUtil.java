package de.take_weiland.mods.commons.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author diesieben07
 */
public class SerializerUtil {

	public static Method findDeserializer(Class<?> clazz, Class<? extends Annotation> annotation, Class<?> paramType) {
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
