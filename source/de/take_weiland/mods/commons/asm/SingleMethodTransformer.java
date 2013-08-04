package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


/**
 * ClassTransformer to transform a single method
 * @author diesieben07
 *
 */
public abstract class SingleMethodTransformer extends MethodTransformer {
	
	@Override
	protected final boolean transforms(ClassNode clazz, MethodNode method) {
		return method.name.equals(getMcpMethod()) || ASMUtils.deobfuscate(clazz.name, method).equals(getSrgMethod());
	}

	/**
	 * Get the MCP name of the method to be transformed (e.g. <tt>onEntityUpdate</tt>)
	 * @return the MCP name
	 */
	protected abstract String getMcpMethod();
	
	/**
	 * Get the Srg name of the method to be transformed (e.g. <tt>func_12345_g</tt>)
	 * @return the Srg name
	 */
	protected abstract String getSrgMethod();
				
}
