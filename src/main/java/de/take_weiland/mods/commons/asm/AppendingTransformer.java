package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * SingleMethodTransformer that appends instructions to the method
 * @author diesieben07
 *
 */
public abstract class AppendingTransformer extends SingleMethodTransformer {

	@Override
	protected final boolean transform(ClassNode clazz, MethodNode method) {
		method.instructions.insertBefore(ASMUtils.findLastReturn(method), getAppends(clazz, method));
		return true;
	}
	
	/**
	 * create a list of instructions which should be appended to the given method
	 * @param clazz the class being transformed
	 * @param method the method being transformed
	 * @return an {@link InsnList} containing the instructions to be appended
	 */
	protected abstract InsnList getAppends(ClassNode clazz, MethodNode method);
}
