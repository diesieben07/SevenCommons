package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

/**
 * SingleMethodTransformer that prepends instructions before the method code
 * @author diesieben07
 *
 */
public abstract class PrependingTransformer extends SingleMethodTransformer {

	@Override
	protected boolean transform(ClassNode clazz, MethodNode method) {
		method.instructions.insert(getPrepends(clazz, method));
		return true;
	}
	
	/**
	 * create a list of instructions which should be prepended to the given method
	 * @param clazz the class being transformed
	 * @param method the method being transformed
	 * @return an {@link InsnList} containing the instructions to be prepended
	 */
	protected abstract InsnList getPrepends(ClassNode clazz, MethodNode method);
}
