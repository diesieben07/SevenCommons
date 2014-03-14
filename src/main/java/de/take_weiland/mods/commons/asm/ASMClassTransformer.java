package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;

/**
 * A ClassTransformer that uses ASM to transform the class. Use a subclass of {@link de.take_weiland.mods.commons.asm.ASMClassTransformerWrapper}
 * to use it with FML.
 *
 * @author diesieben07
 */
public interface ASMClassTransformer {

	/**
	 * Transform the given ClassNode. Only called whne {@link #transforms(String)} returns true for the Class name
	 *
	 * @param clazz the class to transform
	 * @param classInfo a {@link de.take_weiland.mods.commons.asm.ClassInfo} instance representing the class
	 * @return true if the class was transformed
	 */
	boolean transform(ClassNode clazz, ClassInfo classInfo);

	/**
	 * return true if the given class name is to be transformed by this ClassTransformer.
	 * @param internalName the internal name of the class (e.g. "java/lang/Object")
	 * @return true if {@link #transform(org.objectweb.asm.tree.ClassNode, ClassInfo)} should be called for this class
	 */
	boolean transforms(String internalName);

}
