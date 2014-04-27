package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import static org.objectweb.asm.tree.AbstractInsnNode.FRAME;
import static org.objectweb.asm.tree.AbstractInsnNode.LINE;

/**
 * @author diesieben07
 */
class InsnListMatcher extends AbstractMatcher {

	private final InsnList insns;

	InsnListMatcher(InsnList insns) {
		this.insns = insns;
	}

	@Override
	protected AbstractInsnNode matchEndPoint(AbstractInsnNode start) {
		AbstractInsnNode model = insns.getFirst();
		AbstractInsnNode target = start;
		while (true) {
			if (target == null) {
				return null;
			}

			boolean skip;
			if (!ASMUtils.matches(model, target)) {
				if (canSkip(model)) {
					skip = true;
				} else {
					return null;
				}
			} else {
				skip = false;
			}

			model = model.getNext();
			if (model == null) {
				// we've reached the end of our list, so all instructions have matched
				return target;
			}

			// if we have skipped the current insn, don't advance the target
			if (!skip) {
				target = target.getNext();
			}
		}
	}

	private static boolean canSkip(AbstractInsnNode insn) {
		int type = insn.getType();
		return type == LINE || type == FRAME;
	}

	@Override
	protected int size() {
		return insns.size();
	}
}
