package de.take_weiland.mods.commons.asm.info;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author diesieben07
 */
final class ClassInfoReflect extends ClassInfo {

	private final Class<?> clazz;
	private List<String> interfaces;

	ClassInfoReflect(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public List<String> interfaces() {
		if (interfaces == null) {
			ImmutableList.Builder<String> builder = ImmutableList.builder();
			for (Class<?> iface : clazz.getInterfaces()) {
				builder.add(Type.getInternalName(iface));
			}
			interfaces = builder.build();
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
	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return clazz.isAnnotationPresent(annotation);
	}

	@Override
	public AnnotationInfo getAnnotation(Class<? extends Annotation> annotation) {
		Annotation ann = clazz.getAnnotation(annotation);
		return ann == null ? null : new AnnotationInfoReflect(ann);
	}

	@Override
	public boolean hasMemberAnnotation(Class<? extends Annotation> annotation) {
		for (Field field : clazz.getFields()) {
			if (field.isAnnotationPresent(annotation)) {
				return true;
			}
		}

		for (Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				return true;
			}
		}
		return false;
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
	public Type getComponentType() {
		if (clazz.isArray()) {
			return Type.getType(clazz.getComponentType());
		} else {
			throw new IllegalStateException("Not an array");
		}
	}

	@Override
	public boolean hasField(String name) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public FieldInfo getField(String name) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().equals(name)) {
				return new FieldInfoReflect(this, field);
			}
		}
		return null;
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

	@Override
	public boolean hasConstructor(String desc) {
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (Type.getConstructorDescriptor(constructor).equals(desc)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MethodInfo getConstructor(String desc) {
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (Type.getConstructorDescriptor(constructor).equals(desc)) {
				return new MethodInfoReflectCstr(this, constructor);
			}
		}
		return null;
	}

	private List<MethodInfo> methods;
	@Override
	public List<MethodInfo> getMethods() {
		if (methods == null) {
			ImmutableList.Builder<MethodInfo> builder = ImmutableList.builder();
			for (Method method : clazz.getDeclaredMethods()) {
				builder.add(new MethodInfoReflect(this, method));
			}
			methods = builder.build();
		}
		return methods;
	}

	private List<MethodInfo> constructors;
	@Override
	public List<MethodInfo> getConstructors() {
		if (constructors == null) {
			ImmutableList.Builder<MethodInfo> builder = ImmutableList.builder();
			for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
				builder.add(new MethodInfoReflectCstr(this, constructor));
			}
			constructors = builder.build();
		}
		return constructors;
	}

	private List<FieldInfo> fields;
	@Override
	public List<FieldInfo> getFields() {
		if (fields == null) {
			ImmutableList.Builder<FieldInfo> builder = ImmutableList.builder();
			for (Field field : clazz.getDeclaredFields()) {
				builder.add(new FieldInfoReflect(this, field));
			}
			fields = builder.build();
		}
		return fields;
	}

	@Override
	boolean callRightAssignableFrom(ClassInfo parent) {
		return parent.isAssignableFromReflect(this);
	}

	@Override
	boolean isAssignableFromReflect(ClassInfoReflect child) {
		return this.clazz.isAssignableFrom(child.clazz);
	}

}
