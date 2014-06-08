package de.take_weiland.mods.commons.asm;

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
	public void insertBefore(InsnList list, AbstractInsnNode location) {
		if (location == list.getFirst()) {
			list.insert(ASMUtils.clone(insns, newContext()));
		} else {
			list.insertBefore(location, ASMUtils.clone(insns, newContext()));
		}
	}

	@Override
	public void insertAfter(InsnList list, AbstractInsnNode location) {
		if (location == list.getLast()) {
			list.add(ASMUtils.clone(insns, newContext()));
		} else {
			list.insert(location, ASMUtils.clone(insns, newContext()));
		}
	}

	@Override
	public int size() {
		return insns.size();
	}

}
