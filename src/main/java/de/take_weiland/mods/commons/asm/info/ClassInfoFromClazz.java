package de.take_weiland.mods.commons.asm.info;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
* @author diesieben07
*/
final class ClassInfoFromClazz extends ClassInfo {

	private final Class<?> clazz;
	private List<String> interfaces;

	ClassInfoFromClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public List<String> interfaces() {
		if (interfaces == null) {
			interfaces = Lists.transform(Arrays.asList(clazz.getInterfaces()), ClassToNameFunc.INSTANCE);
		}
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
	public boolean hasMethod(String name) {
		if (name.equals("<init>")) {
			// classes always have at least one constructor
			return true;
		}
		// can't check for <clinit> here sadly (?)
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasMethod(String name, String desc) {
		if (name.equals("<init>")) {
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				if (desc.equals(Type.getConstructorDescriptor(c))) {
					return true;
				}
			}
		} else {
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getName().equals(name) && desc.equals(Type.getMethodDescriptor(m))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public MethodInfo getMethod(String name) {
		if (name.equals("<init>")) {
			return new MethodInfoReflectCstr(this, clazz.getDeclaredConstructors()[0]);
		} else {
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getName().equals(name)) {
					return new MethodInfoReflect(this, m);
				}
			}
		}
		return null;
	}

	@Override
	public MethodInfo getMethod(String name, String desc) {
		if (name.equals("<init>")) {
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				if (Type.getConstructorDescriptor(c).equals(desc)) {
					return new MethodInfoReflectCstr(this, c);
				}
			}
		} else {
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getName().equals(name) && Type.getMethodDescriptor(m).equals(desc)) {
					return new MethodInfoReflect(this, m);
				}
			}
		}
		return null;
	}

	private static enum ClassToNameFunc implements Function<Class<?>, String> {
		INSTANCE;

		@Override
		public String apply(Class<?> input) {
			return Type.getInternalName(input);
		}
	}
}
