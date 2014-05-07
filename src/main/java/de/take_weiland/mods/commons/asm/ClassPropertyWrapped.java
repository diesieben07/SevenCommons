package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.annotation.ElementType;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID;

/**
 * @author diesieben07
 */
class ClassPropertyWrapped extends AbstractClassProperty {

	private final ClassNode clazz;
	private final MethodNode getter;
	private final MethodNode setter;

	ClassPropertyWrapped(ClassNode clazz, MethodNode getter, MethodNode setter) {
		Type valueType = Type.getReturnType(getter.desc);
		checkArgument(valueType.getSort() != VOID, "getter must not return void!");
		checkArgument(Type.getArgumentTypes(getter.desc).length == 0, "getter must not take arguments");

		if (setter != null) {
			checkArgument((getter.access & ACC_STATIC) == (setter.access & ACC_STATIC), "setter and getter must have the same static-state");
			Type[] setterArgs = Type.getArgumentTypes(setter.desc);
			checkArgument(setterArgs.length == 1, "setter must only take one argument");
			checkArgument(Type.getReturnType(setter.desc).getSort() == VOID, "setter must return void");
			checkArgument(setterArgs[0].equals(valueType), "setter takes wrong argument!");
		}

		this.clazz = clazz;
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	CodePiece makeGet() {
		InsnList insns = new InsnList();
		int invokeOp;
		if ((getter.access & ACC_STATIC) != ACC_STATIC) {
			insns.add(new VarInsnNode(ALOAD, 0));
			invokeOp = (setter.access & ACC_PRIVATE) == ACC_PRIVATE ? INVOKESPECIAL : INVOKEVIRTUAL;
		} else {
			invokeOp = INVOKESTATIC;
		}
		insns.add(new MethodInsnNode(invokeOp, clazz.name, getter.name, getter.desc));
		return ASMUtils.asCodePiece(insns);
	}

	@Override
	CodePiece makeSet(CodePiece loadValue) {
		InsnList insns = new InsnList();
		int invokeOp;
		if ((setter.access & ACC_STATIC) != ACC_STATIC) {
			insns.add(new VarInsnNode(ALOAD, 0));
			invokeOp = (setter.access & ACC_PRIVATE) == ACC_PRIVATE ? INVOKESPECIAL : INVOKEVIRTUAL;
		} else {
			invokeOp = PUTSTATIC;
		}
		loadValue.appendTo(insns);
		insns.add(new MethodInsnNode(invokeOp, clazz.name, setter.name, setter.desc));
		return ASMUtils.asCodePiece(insns);
	}

	@Override
	public boolean isWritable() {
		return setter != null;
	}

	@Override
	Type makeType() {
		return Type.getReturnType(getter.desc);
	}

	@Override
	List<AnnotationNode> getterAnns(boolean visible) {
		return visible ? getter.visibleAnnotations : getter.invisibleAnnotations;
	}

	@Override
	List<AnnotationNode> setterAnns(boolean visible) {
		return visible ? setter.visibleAnnotations : setter.invisibleAnnotations;
	}

	@Override
	ElementType annotationType() {
		return ElementType.METHOD;
	}

	@Override
	int setterModifiers() {
		return setter.access;
	}

	@Override
	int getterModifiers() {
		return getter.access;
	}

	@Override
	public String propertyName() {
		return getter.name.startsWith("get") ? getter.name.substring(3) : getter.name;
	}

	@Override
	public String toString() {
		if (isWritable()) {
			return String.format("Getter/Setter Pair (getter=\"%s\", setter=\"%s\"", getter.name, setter.name);
		} else {
			return "Getter \"" + getter.name + "\"";
		}
	}
}
