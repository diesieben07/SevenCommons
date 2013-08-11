package de.take_weiland.mods.commons.asm;


import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;


/**
 * IClassTransformer that selects the classes to be transformed via their name
 * @author diesieben07
 *
 */
public abstract class SelectiveTransformer implements IClassTransformer {

	@Override
	public final byte[] transform(String name, String transformedName, byte[] bytes) {
		if (transforms(transformedName)) {
			ClassNode clazz = ASMUtils.getClassNode(bytes);
			
			if (transform(clazz)) {
				
				System.out.println("Transforming class " + transformedName);
				
				ClassWriter writer = new ExtendedClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
				clazz.accept(writer);
				bytes = writer.toByteArray();
			}
		}
		return bytes;
	}
	
	protected abstract boolean transforms(String className);
	
	protected abstract boolean transform(ClassNode clazz);
	
}
