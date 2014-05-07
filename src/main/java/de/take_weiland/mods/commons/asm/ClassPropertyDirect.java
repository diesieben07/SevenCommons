package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.annotation.ElementType;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
class ClassPropertyDirect extends AbstractClassProperty {

	private final ClassNode clazz;
	private final FieldNode field;

	ClassPropertyDirect(ClassNode clazz, FieldNode field) {
		this.clazz = clazz;
		this.field = field;
	}

	@Override
	CodePiece makeSet(CodePiece loadValue) {
		InsnList insns = new InsnList();
		int setOp;
		if ((field.access & ACC_STATIC) != ACC_STATIC) {
			insns.add(new VarInsnNode(ALOAD, 0));
			setOp = PUTFIELD;
		} else {
			setOp = PUTSTATIC;
		}
		loadValue.appendTo(insns);
		insns.add(new FieldInsnNode(setOp, clazz.name, field.name, field.desc));
		return ASMUtils.asCodePiece(insns);
	}

	CodePiece makeGet() {
		InsnList insns = new InsnList();
		int getOp;
		if ((field.access & ACC_STATIC) != ACC_STATIC) {
			insns.add(new VarInsnNode(ALOAD, 0));
			getOp = GETFIELD;
		} else {
			getOp = GETSTATIC;
		}
		insns.add(new FieldInsnNode(getOp, clazz.name, field.name, field.desc));
		return ASMUtils.asCodePiece(insns);
	}

	@Override
	public boolean isWritable() {
		return (field.access & ACC_FINAL) != ACC_FINAL;
	}

	@Override
	Type makeType() {
		return Type.getType(field.desc);
	}

	@Override
	ElementType annotationType() {
		return ElementType.FIELD;
	}

	@Override
	List<AnnotationNode> getterAnns(boolean visible) {
		return visible ? field.visibleAnnotations : field.invisibleAnnotations;
	}

	@Override
	List<AnnotationNode> setterAnns(boolean visible) {
		return getterAnns(visible);
	}

	@Override
	int setterModifiers() {
		return getterModifiers();
	}

	@Override
	int getterModifiers() {
		return field.access;
	}

	@Override
	public String propertyName() {
		return field.name;
	}

	@Override
	public String toString() {
		return "Field \"" + field.name + "\"";
	}
}
