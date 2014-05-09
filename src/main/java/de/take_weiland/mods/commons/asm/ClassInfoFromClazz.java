package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
* @author diesieben07
*/
final class ClassInfoFromClazz extends ClassInfo {

	private final Class<?> clazz;
	private final Collection<String> interfaces;

	ClassInfoFromClazz(Class<?> clazz) {
		this.clazz = clazz;
		interfaces = Collections2.transform(Arrays.asList(clazz.getInterfaces()), ClassToNameFunc.INSTANCE);
	}

	@Override
	public Collection<String> interfaces() {
		return interfaces;
	}

	@Override
	public String superName() {
		Class<?> s = clazz.getSuperclass();
		return s == null ? null : Type.getInternalName(s);
	}

	@Override
	public String internalName() {
		return Type.getInternalName(clazz);
	}

	@Override
	public int modifiers() {
		return clazz.getModifiers();
	}

	@Override
	public boolean isAssignableFrom(ClassInfo child) {
		if (child instanceof ClassInfoFromClazz) {
			return this.clazz.isAssignableFrom(((ClassInfoFromClazz) child).clazz);
		} else {
			return super.isAssignableFrom(child);
		}
	}

	@Override
	public int getDimensions() {
		if (clazz.isArray()) {
			return StringUtils.countMatches(clazz.getName(), "[");
		} else {
			return 0;
		}
	}

	@Override
	public boolean hasMethod(String method) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(method)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasMethod(String method, String desc) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(method) && desc.equals(Type.getMethodDescriptor(m))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MethodInfo getMethod(String method) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(method)) {
				return new MethodInfoReflect(this, m);
			}
		}
		return null;
	}

	@Override
	public MethodInfo getMethod(String method, String desc) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(method) && Type.getMethodDescriptor(m).equals(desc)) {
				return new MethodInfoReflect(this, m);
			}
		}
		return null;
	}

}
