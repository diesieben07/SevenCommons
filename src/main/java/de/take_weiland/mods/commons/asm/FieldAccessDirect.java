package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
class FieldAccessDirect extends AbstractFieldAccess {

	private final ClassNode clazz;
	private final FieldNode field;

	FieldAccessDirect(ClassNode clazz, FieldNode field) {
		this.clazz = clazz;
		this.field = field;
	}

	@Override
	public CodePiece setValue(CodePiece loadValue) {
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
		return true;
	}
}
