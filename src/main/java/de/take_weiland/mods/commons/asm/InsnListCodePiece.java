package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
class InsnListCodePiece extends CodePiece {

	private final InsnList insns;

	InsnListCodePiece(InsnList insns) {
		this.insns = checkNotNull(insns, "InsnList");
	}

	@Override
	void insertBefore0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		InsnList clone = ASMUtils.clone(insns, context.get(contextKey));
		if (location == insns.getFirst()) { // special case for empty list
			insns.insert(clone);
		} else {
			insns.insertBefore(location, clone);
		}
	}

	@Override
	void insertAfter0(InsnList insns, AbstractInsnNode location, Map<ContextKey, Map<LabelNode, LabelNode>> context) {
		InsnList clone = ASMUtils.clone(insns, context.get(contextKey));
		if (location == insns.getLast()) { // special case for empty list
			insns.add(clone);
		} else {
			insns.insert(location, clone);
		}
	}

}
