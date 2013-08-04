package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A {@link SelectiveTransformer} that also selects which methods to transform
 * @author diesieben07
 *
 */
public abstract class MethodTransformer extends SelectiveTransformer {

	@Override
	protected final boolean transform(ClassNode clazz) {
		for (MethodNode method : clazz.methods) {
			if (transforms(clazz, method)) {
				System.out.println("Transforming method " + method.name);
				return transform(clazz, method);
			}
		}
		return false;
	}
	
	/**
	 * if the given MethodNode in the given Class should be transformed
	 * @param clazz the class being transformed
	 * @param method the method being transformed
	 * @return if the method should be transformed
	 */
	protected abstract boolean transforms(ClassNode clazz, MethodNode method);
	
	/**
	 * called to transform the given method in the given class.
	 * @param clazz the class in which the method resides
	 * @param method the method to be transformed
	 * @return true if the method was actually transformed
	 */
	protected abstract boolean transform(ClassNode clazz, MethodNode method);

}
