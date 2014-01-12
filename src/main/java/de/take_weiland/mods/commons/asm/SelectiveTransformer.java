package de.take_weiland.mods.commons.asm;


import java.io.PrintWriter;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

import de.take_weiland.mods.commons.asm.transformers.SyncingTransformer;


/**
 * IClassTransformer that selects the classes to be transformed via their name
 * @author diesieben07
 *
 */
public abstract class SelectiveTransformer implements IClassTransformer {

	@Override
	public final byte[] transform(String name, String transformedName, byte[] bytes) {
		if (bytes != null && transforms(transformedName)) {
			ClassNode clazz = ASMUtils.getClassNode(bytes);
			
			if (transform(clazz, transformedName)) {
				
				System.out.println("Transforming class " + transformedName);
				
				ClassWriter writer = new ExtendedClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				clazz.accept(writer);
				bytes = writer.toByteArray();
				if (this instanceof SyncingTransformer) {
					CheckClassAdapter.verify(new ClassReader(bytes), false, new PrintWriter(System.out));
				}
			}
		}
		return bytes;
	}
	
	protected abstract boolean transforms(String className);
	
	protected abstract boolean transform(ClassNode clazz, String className);
	
}
