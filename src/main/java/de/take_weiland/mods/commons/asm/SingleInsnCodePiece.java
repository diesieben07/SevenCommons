package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
class SingleInsnCodePiece extends AbstractCodePiece {

	private final AbstractInsnNode insn;

	SingleInsnCodePiece(AbstractInsnNode insn) {
		this.insn = checkNotNull(insn, "InsnNode");
	}

	@Override
	public InsnList build() {
		InsnList insns = new InsnList();
		insns.add(getInsn(insns));
		return insns;
	}

	@Override
	public void insertAfter(InsnList into, AbstractInsnNode location) {
		into.insert(location, getInsn(into));
	}

	@Override
	public void insertBefore(InsnList into, AbstractInsnNode location) {
		into.insertBefore(location, getInsn(into));
	}

	@Override
	public void appendTo(InsnList to) {
		to.add(getInsn(to));
	}

	@Override
	public void prependTo(InsnList to) {
		to.insert(getInsn(to));
	}

	private AbstractInsnNode getInsn(InsnList insns) {
		if (insn instanceof LabelNode) {
			return AbstractCodePiece.cloneFor(insns, ((LabelNode) insn));
		} else {
			return insn.clone(AbstractCodePiece.cloneMapFor(insns));
		}
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void appendTo(MethodVisitor mv) {
		insn.accept(mv);
	}
}
