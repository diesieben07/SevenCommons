package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static com.google.common.base.Preconditions.checkArgument;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.DOUBLE;
import static org.objectweb.asm.Type.VOID;

/**
 * @author diesieben07
 */
class FieldAccessWrapped extends AbstractFieldAccess {

	private final ClassNode clazz;
	private final MethodNode getter;
	private final MethodNode setter;

	FieldAccessWrapped(ClassNode clazz, MethodNode getter, MethodNode setter) {
		checkArgument((getter.access & ACC_STATIC) == (setter.access & ACC_STATIC), "setter and getter must have the same static-state");

		Type valueType = Type.getReturnType(getter.desc);
		checkArgument(valueType.getSort() != VOID, "getter must not return void!");

		Type[] setterArgs = Type.getArgumentTypes(setter.desc);
		checkArgument(setterArgs.length == 1, "setter must only take one argument");
		checkArgument(Type.getReturnType(setter.desc).getSort() == VOID, "setter must return void");
		checkArgument(setterArgs[0].equals(valueType), "setter takes wrong argument!");
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
	CodePiece makeSet() {
		if (!isWritable()) {
			throw new AssertionError();
		}
		InsnList insns = new InsnList();
		int invokeOp;
		if ((setter.access & ACC_STATIC) != ACC_STATIC) {
			// need to push this, then swap arguments on the stack
			insns.add(new VarInsnNode(ALOAD, 0));
			Type fieldType = Type.getArgumentTypes(setter.desc)[0];
			// if we have a double or long, SWAP doesn't work :/
			if (fieldType.getSort() == DOUBLE || fieldType.getSort() == LONG) {
				insns.add(new InsnNode(DUP_X2)); // insert the Objectref above the 2 slots of the double/long
				insns.add(new InsnNode(POP)); // and remove the unneeded one
			} else {
				// can simply swap here
				insns.add(new InsnNode(SWAP));
			}
			invokeOp = (setter.access & ACC_PRIVATE) == ACC_PRIVATE ? INVOKESPECIAL : INVOKEVIRTUAL;
		} else {
			invokeOp = PUTSTATIC;
		}
		insns.add(new MethodInsnNode(invokeOp, clazz.name, setter.name, setter.desc));
		return ASMUtils.asCodePiece(insns);
	}

	@Override
	public boolean isWritable() {
		return setter != null;
	}
}
