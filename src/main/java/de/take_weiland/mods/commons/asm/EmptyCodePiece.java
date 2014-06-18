package de.take_weiland.mods.commons.asm;

import de.take_weiland.mods.commons.util.JavaUtils;
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
	public void replace(CodeLocation location) {
		JavaUtils.clear(location.iterator());
	}

	@Override
	public CodePiece append(AbstractInsnNode node) {
		return CodePieces.of(node);
	}

	@Override
	public CodePiece append(InsnList insns) {
		return CodePieces.of(insns);
	}

	@Override
	public CodePiece append(CodePiece other) {
		return other;
	}

	@Override
	public CodePiece prepend(CodePiece other) {
		return other;
	}

	@Override
	public CodePiece prepend(AbstractInsnNode node) {
		return CodePieces.of(node);
	}

	@Override
	public CodePiece prepend(InsnList insns) {
		return CodePieces.of(insns);
	}

	@Override
	public int size() {
		return 0;
	}

}
