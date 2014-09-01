package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Map;

/**
 * @author diesieben07
 */
final class EmptyCodePiece extends CodePiece {

	public static final EmptyCodePiece INSTANCE = new EmptyCodePiece();

	private EmptyCodePiece() { }

	@Override
	public InsnList build() {
		return new InsnList();
	}

	@Override
	public void insertAfter(InsnList to, AbstractInsnNode location) { }

	@Override
	public void insertBefore(InsnList to, AbstractInsnNode location) { }

	@Override
	void insertBefore0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) { }

	@Override
	void insertAfter0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) { }

	@Override
	CodePiece callRightAppend(CodePiece self) {
		return self;
	}

	@Override
	boolean isEmpty() {
		return true;
	}
}
