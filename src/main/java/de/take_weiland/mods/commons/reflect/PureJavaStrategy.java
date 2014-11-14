package de.take_weiland.mods.commons.reflect;

import com.google.common.reflect.Reflection;

import java.lang.reflect.*;
import java.util.Arrays;

final class PureJavaStrategy extends ReflectionStrategy {

	@Override
	public <T> T createAccessor(Class<T> iface) {
		final InterfaceInfo info = analyze(iface);

		return Reflection.newProxy(iface, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Field toGet = info.getters.get(method);
				if (toGet != null) {
					return Modifier.isStatic(toGet.getModifiers()) ? toGet.get(null) : toGet.get(args[0]);
				}
				Field toSet = info.setters.get(method);
				if (toSet != null) {
					if (Modifier.isStatic(toSet.getModifiers())) {
						toSet.set(null, args[0]);
					} else {
						toSet.set(args[0], args[1]);
					}
					return null;
				}

				Method toInvoke = info.invokers.get(method);
				if (toInvoke != null) {
					Object[] invokeParams = Arrays.copyOfRange(args, 1, args.length);
					return Modifier.isStatic(toInvoke.getModifiers()) ? toInvoke.invoke(null, invokeParams) : toInvoke.invoke(args[0], invokeParams);
				}

				Constructor<?> cstr = info.cstrs.get(method);
				if (cstr != null) {
					return cstr.newInstance(args);
				}

				throw new IllegalStateException(String.format("Something somewhere went wrong, don't know how to handle method %s", method.getName()));
			}
		});
	}

	@Override
	public Class<?> defineDynClass(byte[] clazz, Class<?> context) {
		return new AnonClassLoader(context.getClassLoader()).define(clazz);
	}

	private static class AnonClassLoader extends ClassLoader {

		AnonClassLoader(ClassLoader parent) {
			super(parent);
		}

		Class<?> define(byte[] clazz) {
			return defineClass(null, clazz, 0, clazz.length);
		}

	}

}
