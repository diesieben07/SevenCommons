package de.take_weiland.mods.commons.fastreflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

abstract class AbstractStrategy implements FastreflectStrategy {

	void validateInterface(Class<?> iface) {
		if (!iface.isInterface()) {
			throw new IllegalArgumentException("Interface expected!");
		}
	}
	
	InterfaceInfo analyze(Class<?> iface) {
		validateInterface(iface);
		
		Map<Method, Field> getters = Maps.newHashMap();
		Map<Method, Field> setters = Maps.newHashMap();
		Map<Method, Method> invokers = Maps.newHashMap();
		
		for (Method method : iface.getMethods()) {
			if (method.isAnnotationPresent(Getter.class)) {
				getters.put(method, findGetterTarget(iface, method));
			} else if (method.isAnnotationPresent(Setter.class)) {
				setters.put(method, findSetterTarget(iface, method));
			} else if (method.isAnnotationPresent(Invoke.class)) {
				invokers.put(method, findInvokerTarget(iface, method));
			} else {
				throw new IllegalArgumentException("Don't know what to do with method %s in interface %s");
			}
		}
		return new InterfaceInfo(getters, setters, invokers);
	}
	
	Field findGetterTarget(Class<?> iface, Method getter) {
		Class<?>[] params = getter.getParameterTypes();
		if (params.length != 1 || params[0].isPrimitive()) {
			throw new IllegalArgumentException(String.format("Invalid getter %s in interface %s", getter.getName(), iface.getName()));
		}
		Getter ann = getter.getAnnotation(Getter.class);
		Field f = ReflectionHelper.findField(params[0], ann.field());
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
		Field f = ReflectionHelper.findField(params[0], ann.field());
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
		Method m = findMethod(params[0], ann.method(), Arrays.copyOfRange(params, 1, params.length));
		if (m.getReturnType() != invoker.getReturnType()) {
			throw new IllegalArgumentException(String.format("Mismatched return type on %s in %s", invoker.getName(), iface.getName()));
		}
		return m;
	}
	
	// copied and fixed from FMLs helper
	Method findMethod(Class<?> clazz, String[] methodNames, Class<?>... methodTypes) {
		Exception failed = null;
		for (String methodName : methodNames) {
			try {
				Method m = clazz.getDeclaredMethod(methodName, methodTypes);
				m.setAccessible(true);
				return m;
			} catch (Exception e) {
				failed = e;
			}
		}
		throw new UnableToFindMethodException(methodNames, failed);
	}
	
	static class InterfaceInfo {
		
		final Map<Method, Field> getters;
		final Map<Method, Field> setters;
		final Map<Method, Method> invokers;
		
		InterfaceInfo(Map<Method, Field> getters, Map<Method, Field> setters, Map<Method, Method> invokers) {
			this.setters = setters;
			this.getters = getters;
			this.invokers = invokers;
		}
		
	}
	
}
