package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.reflect.SCReflection;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * @author diesieben07
 */
final class TransformerUtil {


	static AbstractInsnNode requireNext(AbstractInsnNode node) {
		if ((node = node.getNext()) == null) {
			throw new IllegalStateException("Missing next node in " + SCReflection.getCallerClass().getSimpleName());
		}
		return node;
	}

	static AbstractInsnNode requirePrev(AbstractInsnNode node) {
		if ((node = node.getPrevious()) == null) {
			throw new IllegalStateException("Missing previous node in " + SCReflection.getCallerClass().getSimpleName());
		}
		return node;
	}


}
