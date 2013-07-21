package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import de.take_weiland.mods.commons.util.ASMUtils;

/**
 * ClassTransformer to transform a single method
 * @author diesieben07
 *
 */
public abstract class MethodTransformer extends SelectiveTransformer {

	@Override
	protected final boolean transform(ClassNode clazz) {
		for (MethodNode method : clazz.methods) {
			if (method.name.equals(getMcpMethod()) || ASMUtils.deobfuscate(clazz.name, method).equals(getSrgMethod())) {
				System.out.println("Transforming method " + getMcpMethod());
				return transform(clazz, method);
			}
		}
		return false;
	}
	
	protected abstract String getMcpMethod();
	
	protected abstract String getSrgMethod();
				
	protected abstract boolean transform(ClassNode clazz, MethodNode method);
}
