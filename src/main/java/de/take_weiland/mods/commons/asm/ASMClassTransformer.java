package de.take_weiland.mods.commons.asm;

import de.take_weiland.mods.commons.asm.info.ClassInfo;
import org.objectweb.asm.tree.ClassNode;

/**
 * <p>A ClassTransformer that uses the ASM library to transform classes. Use a subclass of {@link de.take_weiland.mods.commons.asm.ASMClassTransformerWrapper}
 * to use it with FML.</p>
 *
 * @author diesieben07
 */
public interface ASMClassTransformer {

	/**
	 * <p>Transform the given ClassNode. Only called when {@link #transforms(String)} returns true for the class name.</p>
	 *
	 * @param clazz     the class to transform
	 * @param classInfo a {@link de.take_weiland.mods.commons.asm.info.ClassInfo} instance representing the class
	 * @return true if the class was transformed
	 */
	boolean transform(ClassNode clazz, ClassInfo classInfo);

	/**
	 * <p>Check if the given class should be transformed by this ClassTransformer (that is passed to {@link #transform(org.objectweb.asm.tree.ClassNode, ClassInfo)}.</p>
	 *
	 * @param internalName the internal name of the class (e.g. "java/lang/Object")
	 * @return true if {@link #transform(org.objectweb.asm.tree.ClassNode, ClassInfo)} should be called for this class
	 */
	boolean transforms(String internalName);

}
