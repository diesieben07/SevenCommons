package de.take_weiland.mods.commons.asm;

import com.google.common.base.Predicate;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.Iterator;

/**
 * @author diesieben07
 */
class SingleInsnMatcher extends AbstractMatcher {

	private final Predicate<AbstractInsnNode> predicate;

	SingleInsnMatcher(Predicate<AbstractInsnNode> predicate) {
		this.predicate = predicate;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	protected AbstractInsnNode matchEndPoint(InsnList list, AbstractInsnNode start) {
		Iterator<AbstractInsnNode> it = ASMUtils.fastIterator(list, start);
		while (it.hasNext()) {
			AbstractInsnNode insn = it.next();
			if (predicate.apply(insn)) {
				System.out.println("found match at " + list.indexOf(insn));
				return insn;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "SingleInsnMatcher(" + predicate + ')';
	}
}
