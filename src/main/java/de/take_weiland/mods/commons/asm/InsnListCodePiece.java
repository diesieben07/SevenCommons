package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
class InsnListCodePiece extends AbstractCodePiece {

	private final InsnList insns;

	InsnListCodePiece(InsnList insns) {
		this.insns = checkNotNull(insns, "InsnList");
	}

	@Override
	public InsnList build() {
		return ASMUtils.clone(insns, cloneMapFor(insns));
	}

	@Override
	public int size() {
		return insns.size();
	}

	@Override
	public void appendTo(MethodVisitor mv) {
		insns.accept(mv);
	}

	@Override
	public CodePiece append(AbstractInsnNode node) {
		insns.add(node);
		return this;
	}

	@Override
	public CodePiece append(InsnList insns) {
		this.insns.add(insns);
		return this;
	}

	@Override
	public CodePiece prepend(AbstractInsnNode node) {
		insns.insert(node);
		return this;
	}

	@Override
	public CodePiece prepend(InsnList insns) {
		this.insns.insert(insns);
		return this;
	}
}
