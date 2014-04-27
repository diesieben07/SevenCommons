package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author diesieben07
 */
class SingleInsnMatcher extends AbstractMatcher {

	private final AbstractInsnNode model;

	SingleInsnMatcher(AbstractInsnNode model) {
		this.model = model;
	}

	@Override
	protected int size() {
		return 1;
	}

	@Override
	protected AbstractInsnNode matchEndPoint(AbstractInsnNode start) {
		return ASMUtils.matches(start, model) ? start : null;
	}
}
