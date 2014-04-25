package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.DOUBLE;

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

	CodePiece makeSet() {
		return new AbstractCodePiece() {
			@Override
			public InsnList build() {
				InsnList insns = new InsnList();
				int setOp;
				if ((field.access & ACC_STATIC) != ACC_STATIC) {
					// need to push this, then swap arguments on the stack
					insns.add(new VarInsnNode(ALOAD, 0));
					Type fieldType = Type.getType(field.desc);
					// if we have a double or long, SWAP doesn't work :/
					if (fieldType.getSort() == DOUBLE || fieldType.getSort() == LONG) {
						insns.add(new InsnNode(DUP_X2)); // insert the Objectref above the 2 slots of the double/long
						insns.add(new InsnNode(POP)); // and remove the unneeded one
					} else {
						// can simply swap here
						insns.add(new InsnNode(SWAP));
					}
					setOp = PUTFIELD;
				} else {
					setOp = PUTSTATIC;
				}
				insns.add(new FieldInsnNode(setOp, clazz.name, field.name, field.desc));
				return insns;
			}
		};
	}

	CodePiece makeGet() {
		return new AbstractCodePiece() {
			@Override
			public InsnList build() {
				InsnList insns = new InsnList();
				int getOp;
				if ((field.access & ACC_STATIC) != ACC_STATIC) {
					insns.add(new VarInsnNode(ALOAD, 0));
					getOp = GETFIELD;
				} else {
					getOp = GETSTATIC;
				}
				insns.add(new FieldInsnNode(getOp, clazz.name, field.name, field.desc));
				return insns;
			}
		};
	}

	@Override
	public boolean isWritable() {
		return true;
	}
}
