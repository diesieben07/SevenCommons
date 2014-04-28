package org.objectweb.asm.tree;

/**
 * @author diesieben07
 */
public final class SCASMAccessHook {

	public static AbstractInsnNode[] getCache(InsnList list) {
		return list.cache;
	}

	public static int getIndex(AbstractInsnNode node) {
		return node.index;
	}

}
