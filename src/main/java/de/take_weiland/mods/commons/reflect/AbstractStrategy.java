package de.take_weiland.mods.commons.reflect;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToFindMethodException;
import de.take_weiland.mods.commons.asm.MCPNames;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

abstract class AbstractStrategy implements ReflectionStrategy {

	void validateInterface(Class<?> iface) {
		if (!iface.isInterface()) {
			throw new IllegalArgumentException("Interface expected!");
		}
	}

	InterfaceInfo analyze(Class<?> iface) {
		validateInterface(iface);

		ImmutableMap.Builder<Method, Field> getters = ImmutableMap.builder();
		ImmutableMap.Builder<Method, Field> setters = ImmutableMap.builder();
		ImmutableMap.Builder<Method, Method> invokers = ImmutableMap.builder();
		ImmutableMap.Builder<Method, Constructor<?>> cstrs = ImmutableMap.builder();

		for (Method method : iface.getMethods()) {
			if (method.isAnnotationPresent(Getter.class)) {
				getters.put(method, findGetterTarget(iface, method));
			} else if (method.isAnnotationPresent(Setter.class)) {
				setters.put(method, findSetterTarget(iface, method));
			} else if (method.isAnnotationPresent(Invoke.class)) {
				invokers.put(method, findInvokerTarget(iface, method));
			} else if (method.isAnnotationPresent(Construct.class)) {
				cstrs.put(method, findConstructor(method, method.getAnnotation(Construct.class)));
			} else {
				throw new IllegalArgumentException(String.format("Don't know what to do with method %s in interface %s", method.getName(), iface.getName()));
			}
		}
		return new InterfaceInfo(getters.build(), setters.build(), invokers.build(), cstrs.build());
	}

	Field findGetterTarget(Class<?> iface, Method getter) {
		Class<?>[] params = getter.getParameterTypes();
		if (params.length != 1 || params[0].isPrimitive()) {
			throw new IllegalArgumentException(String.format("Invalid getter %s in interface %s", getter.getName(), iface.getName()));
		}
		Getter ann = getter.getAnnotation(Getter.class);
		Field f = ReflectionHelper.findField(params[0], ann.srg() ? MCPNames.field(ann.field()) : ann.field());
		if (f.getType() != getter.getReturnType()) {
			throw new IllegalArgumentException(String.format("Wrong return type for getter %s in %s", getter.getName(), iface.getName()));
		}
		return f;
	}

	Field findSetterTarget(Class<?> iface, Method setter) {
		Class<?>[] params = setter.getParameterTypes();
		if (params.length != 2 || params[0].isPrimitive() || setter.getReturnType() != void.class) {
			throw new IllegalArgumentException(String.format("Invalid setter %s in interface %s", setter.getName(), iface.getName()));
		}
		Setter ann = setter.getAnnotation(Setter.class);
		Field f = ReflectionHelper.findField(params[0], ann.srg() ? MCPNames.field(ann.field()) : ann.field());
		if (f.getType() != params[1]) {
			throw new IllegalArgumentException(String.format("Parameter does not match field type for setter %s in %s", setter.getName(), iface.getName()));
		}
		return f;
	}

	Method findInvokerTarget(Class<?> iface, Method invoker) {
		Class<?>[] params = invoker.getParameterTypes();
		if (params.length == 0 || params[0].isPrimitive()) {
			throw new IllegalArgumentException(String.format("Invalid invoker %s in %s", invoker.getName(), iface.getName()));
		}
		Invoke ann = invoker.getAnnotation(Invoke.class);
		Method m = findMethod(params[0], ann.srg() ? MCPNames.method(ann.method()) : ann.method(), Arrays.copyOfRange(params, 1, params.length));
		if (m.getReturnType() != invoker.getReturnType()) {
			throw new IllegalArgumentException(String.format("Mismatched return type on %s in %s", invoker.getName(), iface.getName()));
		}
		return m;
	}

	Constructor<?> findConstructor(Method bouncer, Construct annotation) {
		Class<?> target = bouncer.getReturnType();
		try {
			Constructor<?> cstr = target.getDeclaredConstructor(bouncer.getParameterTypes());
			cstr.setAccessible(true);
			return cstr;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(String.format("Failed to find constructor with matching parameters for method %s in %s", bouncer.getName(), bouncer.getDeclaringClass().getName()));
		}
	}

	// copied and fixed from FMLs helper
	Method findMethod(Class<?> clazz, String methodName, Class<?>... methodTypes) {
		Exception failed;
		try {
			Method m = clazz.getDeclaredMethod(methodName, methodTypes);
			m.setAccessible(true);
			return m;
		} catch (Exception e) {
			failed = e;
		}
		throw new UnableToFindMethodException(new String[] { methodName }, failed);
	}

	static class InterfaceInfo {

		final Map<Method, Field> getters;
		final Map<Method, Field> setters;
		final Map<Method, Method> invokers;
		final Map<Method, Constructor<?>> cstrs;

		InterfaceInfo(Map<Method, Field> getters, Map<Method, Field> setters, Map<Method, Method> invokers, Map<Method, Constructor<?>> cstrs) {
			this.setters = setters;
			this.getters = getters;
			this.invokers = invokers;
			this.cstrs = cstrs;
		}

	}

}
