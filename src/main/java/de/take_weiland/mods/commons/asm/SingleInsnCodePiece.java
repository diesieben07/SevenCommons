package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
class SingleInsnCodePiece extends AbstractCodePiece {

	private final AbstractInsnNode insn;
	private boolean used;
	boolean dontClone = false;

	SingleInsnCodePiece(AbstractInsnNode insn) {
		this.insn = checkNotNull(insn, "InsnNode");
	}

	@Override
	public InsnList build() {
		InsnList insns = new InsnList();
		insns.add(getInsn());
		return insns;
	}

	@Override
	public void insertAfter(InsnList into, AbstractInsnNode location) {
		into.insert(location, getInsn());
	}

	@Override
	public void insertBefore(InsnList into, AbstractInsnNode location) {
		into.insertBefore(location, getInsn());
	}

	@Override
	public void appendTo(InsnList to) {
		to.add(getInsn());
	}

	@Override
	public void prependTo(InsnList to) {
		to.insert(getInsn());
	}

	private AbstractInsnNode getInsn() {
		if (!dontClone && used) {
			return ASMUtils.clone(insn);
		} else {
			used = true;
			return insn;
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
