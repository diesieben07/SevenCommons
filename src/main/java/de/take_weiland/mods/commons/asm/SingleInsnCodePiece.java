package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
class SingleInsnCodePiece extends AbstractCodePiece {

	private final AbstractInsnNode insn;
	private boolean used;

	SingleInsnCodePiece(AbstractInsnNode insn) {
		this.insn = checkNotNull(insn, "InsnNode");
	}

	@Override
	public InsnList build() {
		InsnList insns = new InsnList();
		insns.add(get());
		return insns;
	}

	@Override
	public void insertAfter(InsnList into, AbstractInsnNode location) {
		into.insert(location, get());
	}

	@Override
	public void insertBefore(InsnList into, AbstractInsnNode location) {
		into.insertBefore(location, get());
	}

	@Override
	public void appendTo(InsnList to) {
		to.add(get());
	}

	@Override
	public void prependTo(InsnList to) {
		to.insert(get());
	}

	private AbstractInsnNode get() {
		if (used) {
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
}
