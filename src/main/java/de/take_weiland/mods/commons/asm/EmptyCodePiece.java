package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * @author diesieben07
 */
enum EmptyCodePiece implements CodePiece {

	INSTANCE;

	@Override
	public InsnList build() {
		return new InsnList();
	}

	@Override
	public void appendTo(InsnList to) { }

	@Override
	public void prependTo(InsnList to) { }

	@Override
	public void insertAfter(InsnList to, AbstractInsnNode location) { }

	@Override
	public void insertBefore(InsnList to, AbstractInsnNode location) { }

	@Override
	public void insertAfter(CodeLocation location) { }

	@Override
	public void insertBefore(CodeLocation location) { }

	@Override
	public CodePiece append(CodePiece other) {
		return other;
	}

	@Override
	public CodePiece prepend(CodePiece other) {
		return other;
	}

	@Override
	public int size() {
		return 0;
	}

}
