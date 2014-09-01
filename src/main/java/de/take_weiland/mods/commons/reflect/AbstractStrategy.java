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
			OverrideTarget oc = method.getAnnotation(OverrideTarget.class);
			String overrideClass = oc == null ? null : oc.value();

			if (method.isAnnotationPresent(Getter.class)) {
				getters.put(method, findGetterTarget(method, overrideClass));
			} else if (method.isAnnotationPresent(Setter.class)) {
				setters.put(method, findSetterTarget(method, overrideClass));
			} else if (method.isAnnotationPresent(Invoke.class)) {
				invokers.put(method, findInvokerTarget(method, overrideClass));
			} else if (method.isAnnotationPresent(Construct.class)) {
				cstrs.put(method, findConstructor(method, overrideClass));
			} else {
				throw new IllegalArgumentException(String.format("Don't know what to do with method %s in interface %s", method.getName(), iface.getName()));
			}
		}
		return new InterfaceInfo(getters.build(), setters.build(), invokers.build(), cstrs.build());
	}

	Field findGetterTarget(Method ifaceMethod, String overrideClassName) {
		Class<?>[] params = ifaceMethod.getParameterTypes();
		if (params.length != 1) {
			throw invalidGetter(ifaceMethod, "Getters have exactly one parameter");
		}

		Getter ann = ifaceMethod.getAnnotation(Getter.class);
		Class<?> target = selectTarget(params[0], overrideClassName, ifaceMethod, "getter");

		Field f = ReflectionHelper.findField(target, ann.srg() ? MCPNames.field(ann.field()) : ann.field());
		if (!ifaceMethod.getReturnType().isAssignableFrom(f.getType())) {
			throw invalidGetter(ifaceMethod, "Field type is not assignable to return type");
		}
		return f;
	}

	private RuntimeException invalidGetter(Method ifaceMethod, String cause) {
		return invalid(ifaceMethod, "getter", cause);
	}

	Field findSetterTarget(Method ifaceMethod, String overrideClass) {
		Class<?>[] params = ifaceMethod.getParameterTypes();
		if (params.length != 2) {
			throw invalidSetter(ifaceMethod, "Need exactly 2 parameters");
		}
		if (ifaceMethod.getReturnType() != void.class) {
			throw invalidSetter(ifaceMethod, "Setters return nothing");
		}

		Class<?> targetClass = selectTarget(params[0], overrideClass, ifaceMethod, "setter");

		Setter ann = ifaceMethod.getAnnotation(Setter.class);
		Field f = ReflectionHelper.findField(targetClass, ann.srg() ? MCPNames.field(ann.field()) : ann.field());
		if (!f.getType().isAssignableFrom(params[1])) {
			throw invalidSetter(ifaceMethod, "Specified type is not assignable to field type");
		}
		return f;
	}


	private RuntimeException invalidSetter(Method ifaceMethod, String cause) {
		return invalid(ifaceMethod, "setter", cause);
	}

	Method findInvokerTarget(Method ifaceMethod, String overrideClass) {
		Class<?>[] params = ifaceMethod.getParameterTypes();
		Invoke ann = ifaceMethod.getAnnotation(Invoke.class);

		if (params.length == 0) {
			throw invalidInvoker(ifaceMethod, "Missing parameter");
		}

		Class<?> targetClass = selectTarget(params[0], overrideClass, ifaceMethod, "invoker");

		Method m = findMethod(targetClass, ann.srg() ? MCPNames.method(ann.method()) : ann.method(), Arrays.copyOfRange(params, 1, params.length));
		if (!ifaceMethod.getReturnType().isAssignableFrom(m.getReturnType())) {
			throw invalidInvoker(ifaceMethod, "Target method's return type not assignable to specified return type");
		}
		return m;
	}

	private RuntimeException invalidInvoker(Method ifaceMethod, String cause) {
		return invalid(ifaceMethod, "invoker", cause);
	}

	Constructor<?> findConstructor(Method ifaceMethod, String overrideClass) {
		Class<?> target = selectTarget(ifaceMethod.getReturnType(), overrideClass, ifaceMethod, overrideClass);
		if (!ifaceMethod.getReturnType().isAssignableFrom(target)) {
			throw invalid(ifaceMethod, "constructor", "Selected target is not assignable to return type");
		}
		try {
			Constructor<?> cstr = target.getDeclaredConstructor(ifaceMethod.getParameterTypes());
			cstr.setAccessible(true);
			return cstr;
		} catch (NoSuchMethodException e) {
			throw invalid(ifaceMethod, "constructor", "No matching constructor found");
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

	private Class<?> selectTarget(Class<?> def, String override, Method ifaceMethod, String type) {
		Class<?> target;
		if (override == null) {
			target = def;
		} else {
			try {
				target = Class.forName(override);
				if (!def.isAssignableFrom(target)) {
					throw invalid(ifaceMethod, type, "Override class is not assignable to specified target");
				}
			} catch (ClassNotFoundException e) {
				throw invalid(ifaceMethod, type, "Override class not found");
			}
		}
		if (target.isPrimitive()) {
			throw invalid(ifaceMethod, type, "Selected target class is a primitive type");
		}
		return target;
	}

	private RuntimeException invalid(Method ifaceMethod, String type, String cause) {
		throw new IllegalArgumentException(String.format("Illegal %s %s in %s (%s)", type, ifaceMethod.getName(), ifaceMethod.getDeclaringClass().getName(), cause));
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
