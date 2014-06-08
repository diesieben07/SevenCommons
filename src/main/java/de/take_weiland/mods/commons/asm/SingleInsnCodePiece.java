package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

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
	public void insertAfter(InsnList into, AbstractInsnNode location) {
		if (location == into.getLast()) {
			into.add(insn.clone(newContext()));
		} else {
			into.insert(location, insn.clone(newContext()));
		}
	}

	@Override
	public void insertBefore(InsnList into, AbstractInsnNode location) {
		if (location == into.getFirst()) {
			into.insert(insn.clone(newContext()));
		} else {
			into.insertBefore(location, insn.clone(newContext()));
		}
	}

	@Override
	public int size() {
		return 1;
	}

}
