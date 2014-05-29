package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.annotation.ElementType;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
class ASMField extends ClassBoundASMVariable {

	private final FieldNode field;

	ASMField(ClassNode clazz, FieldNode field, CodePiece instance) {
		super(clazz, instance);
		this.field = field;
	}

	private CodePiece getCache;
	@Override
	public CodePiece get() {
		if (getCache == null) {
			InsnList insns = new InsnList();
			int getOp;
			if (!isStatic()) {
				instance.appendTo(insns);
				getOp = GETFIELD;
			} else {
				getOp = GETSTATIC;
			}
			insns.add(new FieldInsnNode(getOp, clazz.name, field.name, field.desc));
			getCache = CodePieces.of(insns);
		}
		return getCache;
	}

	@Override
	public CodePiece set(CodePiece loadValue) {
		InsnList insns = new InsnList();
		int setOp;
		if (!isStatic()) {
			instance.appendTo(insns);
			setOp = PUTFIELD;
		} else {
			setOp = PUTSTATIC;
		}
		loadValue.appendTo(insns);
		insns.add(new FieldInsnNode(setOp, clazz.name, field.name, field.desc));
		return CodePieces.of(insns);
	}

	@Override
	public boolean isWritable() {
		return (field.access & ACC_FINAL) != ACC_FINAL;
	}

	@Override
	public Type getType() {
		return Type.getType(field.desc);
	}

	@Override
	protected ElementType annotationType() {
		return ElementType.FIELD;
	}

	@Override
	protected List<AnnotationNode> getterAnns(boolean visible) {
		return visible ? field.visibleAnnotations : field.invisibleAnnotations;
	}

	@Override
	protected List<AnnotationNode> setterAnns(boolean visible) {
		return getterAnns(visible);
	}

	@Override
	protected int setterModifiers() {
		return field.access;
	}

	@Override
	protected int getterModifiers() {
		return field.access;
	}

	@Override
	public String name() {
		return field.name;
	}

	@Override
	public String toString() {
		return "Field \"" + field.name + "\"";
	}
}
