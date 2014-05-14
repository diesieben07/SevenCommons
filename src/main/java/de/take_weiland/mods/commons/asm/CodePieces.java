package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import static de.take_weiland.mods.commons.asm.ASMUtils.asCodePiece;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getMethodDescriptor;

/**
 * @author diesieben07
 */
public final class CodePieces {

	public static CodePiece instantiate(Class<?> c) {
		return instantiate(Type.getInternalName(c));
	}

	public static CodePiece instantiate(Type t) {
		return instantiate(t.getInternalName());
	}

	public static CodePiece instantiate(String internalName) {
		InsnList insns = new InsnList();
		insns.add(new TypeInsnNode(NEW, internalName));
		insns.add(new InsnNode(DUP));
		insns.add(new MethodInsnNode(INVOKESPECIAL, internalName, "<init>", getMethodDescriptor(VOID_TYPE)));
		return asCodePiece(insns);
	}

	public static CodePiece castTo(Type type) {
		return castTo(type.getInternalName());
	}

	public static CodePiece castTo(String internalName) {
		return ASMUtils.asCodePiece(new TypeInsnNode(CHECKCAST, internalName));
	}

}
