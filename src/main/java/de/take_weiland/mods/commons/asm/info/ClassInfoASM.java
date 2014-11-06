package de.take_weiland.mods.commons.asm.info;

import com.google.common.collect.ImmutableList;
import de.take_weiland.mods.commons.asm.ASMUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
final class ClassInfoASM extends ClassInfo {

	private final ClassNode clazz;

	ClassInfoASM(ClassNode clazz) {
		this.clazz = checkNotNull(clazz, "ClassNode");
	}

	@Override
	public List<String> interfaces() {
		return clazz.interfaces;
	}

	@Override
	public String superName() {
		return clazz.superName;
	}

	@Override
	public String internalName() {
		return clazz.name;
	}

	@Override
	public int modifiers() {
		return clazz.access;
	}

	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return ASMUtils.hasAnnotation(clazz, annotation);
	}

	@Override
	public AnnotationInfo getAnnotation(Class<? extends Annotation> annotation) {
		AnnotationNode ann = ASMUtils.getAnnotation(clazz, annotation);
		return ann == null ? null : new AnnotationInfoASM(this, ann);
	}

	@Override
	public int getDimensions() {
		// we never load array classes as a ClassNode
		return 0;
	}

	@Override
	public Type getComponentType() {
		throw new IllegalStateException("Not an array");
	}

	@Override
	public boolean hasField(String name) {
		return ASMUtils.findField(clazz, name) != null;
	}

	@Override
	public FieldInfo getField(String name) {
		FieldNode field = ASMUtils.findField(clazz, name);
		return field != null ? new FieldInfoASM(this, field) : null;
	}

	@Override
	public boolean hasMethod(String name) {
		return !isHidden(name) && ASMUtils.findMethod(clazz, name) != null;
	}

	@Override
	public boolean hasMethod(String name, String desc) {
		return !isHidden(name) && ASMUtils.findMethod(clazz, name, desc) != null;
	}

	@Override
	public MethodInfo getMethod(String name) {
		if (isHidden(name)) {
			return null;
		}
		MethodNode m = ASMUtils.findMethod(clazz, name);
		return m == null ? null : new MethodInfoASM(this, m);
	}

	@Override
	public MethodInfo getMethod(String name, String desc) {
		if (isHidden(name)) {
			return null;
		}
		MethodNode m = ASMUtils.findMethod(clazz, name, desc);
		return m == null ? null : new MethodInfoASM(this, m);
	}

	@Override
	public boolean hasConstructor(String desc) {
		return ASMUtils.findMethod(clazz, "<init>", desc) != null;
	}

	@Override
	public MethodInfo getConstructor(String desc) {
		MethodNode c = ASMUtils.findMethod(clazz, "<init>", desc);
		return c == null ? null : new MethodInfoASM(this, c);
	}

	private static boolean isHidden(String name) {
		return name.equals("<init>") || name.equals("<clinit>");
	}

	@Override
	public List<MethodInfo> getMethods() {
		ImmutableList.Builder<MethodInfo> builder = ImmutableList.builder();

		for (MethodNode method : clazz.methods) {
			if (!isHidden(method.name)) {
				builder.add(new MethodInfoASM(this, method));
			}
		}

		return builder.build();
	}

	@Override
	public List<MethodInfo> getConstructors() {
		ImmutableList.Builder<MethodInfo> builder = ImmutableList.builder();

		for (MethodNode method : clazz.methods) {
			if (method.name.equals("<init>")) {
				builder.add(new MethodInfoASM(this, method));
			}
		}

		return builder.build();
	}

	@Override
	public List<FieldInfo> getFields() {
		ImmutableList.Builder<FieldInfo> builder = ImmutableList.builder();

		for (FieldNode field : clazz.fields) {
			builder.add(new FieldInfoASM(this, field));
		}

		return builder.build();
	}
}
