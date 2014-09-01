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
			if (!isStatic()) {
				getCache = CodePieces.getField(clazz, field, instance);
			} else {
				getCache = CodePieces.getField(clazz, field);
			}
		}
		return getCache;
	}

	@Override
	public CodePiece set(CodePiece newValue) {
		if (isStatic()) {
			return CodePieces.setField(clazz, field, newValue);
		} else {
			return CodePieces.setField(clazz, field, instance, newValue);
		}
	}

	@Override
	public CodePiece setAndGet(CodePiece newValue) {
		CodeBuilder cb = new CodeBuilder();

		if (isStatic()) {
			cb.add(newValue);
			cb.add(new InsnNode(DUP));
			cb.add(new FieldInsnNode(PUTSTATIC, clazz.name, field.name, field.desc));
		} else {
			cb.add(instance);
			cb.add(newValue);
			cb.add(new InsnNode(DUP_X1));
			cb.add(new FieldInsnNode(PUTFIELD, clazz.name, field.name, field.desc));
		}

		return cb.build();
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

	@Override
	public boolean isField() {
		return true;
	}

	@Override
	public boolean isMethod() {
		return false;
	}
}
