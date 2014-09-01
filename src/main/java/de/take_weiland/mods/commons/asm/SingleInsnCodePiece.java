package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
class SingleInsnCodePiece extends CodePiece {

	private final AbstractInsnNode insn;

	SingleInsnCodePiece(AbstractInsnNode insn) {
		this.insn = checkNotNull(insn, "InsnNode");
	}

	@Override
	void insertBefore0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		AbstractInsnNode clone = insn.clone(context.get(contextKey));
		if (location == insns.getFirst()) {
			insns.insert(clone);
		} else {
			insns.insertBefore(location, clone);
		}
	}

	@Override
	void insertAfter0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		AbstractInsnNode clone = insn.clone(context.get(contextKey));
		if (location == insns.getLast()) {
			insns.add(clone);
		} else {
			insns.insert(location, clone);
		}
	}

	@Override
	boolean isEmpty() {
		return false;
	}
}
