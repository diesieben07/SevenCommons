package de.take_weiland.mods.commons.asm;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

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
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasMethod(String name, String desc) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(name) && desc.equals(Type.getMethodDescriptor(m))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MethodInfo getMethod(String name) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(name)) {
				return new MethodInfoReflect(this, m);
			}
		}
		return null;
	}

	@Override
	public MethodInfo getMethod(String name, String desc) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(name) && Type.getMethodDescriptor(m).equals(desc)) {
				return new MethodInfoReflect(this, m);
			}
		}
		return null;
	}

	private List<Type[]> constructors;

	private List<Type[]> constructorsVisible;

	private static enum ClassToNameFunc implements Function<Class<?>, String> {
		INSTANCE;

		@Override
		public String apply(Class<?> input) {
			return Type.getInternalName(input);
		}
	}
}
