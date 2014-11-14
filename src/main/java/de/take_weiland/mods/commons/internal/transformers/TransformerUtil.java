package de.take_weiland.mods.commons.internal.transformers;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author diesieben07
 */
final class TransformerUtil {


	static AbstractInsnNode requireNext(AbstractInsnNode node) {
		if ((node = node.getNext()) == null) {
			throw new IllegalStateException("Missing next node");
		}
		return node;
	}

	static AbstractInsnNode requirePrev(AbstractInsnNode node) {
		if ((node = node.getPrevious()) == null) {
			throw new IllegalStateException("Missing previous node");
		}
		return node;
	}


}
