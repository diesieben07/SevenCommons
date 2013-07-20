package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public abstract class ClassTransformer implements IClassTransformer {

	@Override
	public final byte[] transform(String name, String transformedName, byte[] bytes) {
		if (transformedName.equals(getClassName())) {
			ClassReader reader = new ClassReader(bytes);
			ClassNode clazz = new ClassNode();
			reader.accept(clazz, 0);
			
			System.out.println("Transforming class " + transformedName);
			
			if (transform(clazz)) {
				
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				clazz.accept(writer);
				bytes = writer.toByteArray();
			}
		}
		return bytes;
	}
	
	protected abstract String getClassName();
	
	protected abstract boolean transform(ClassNode clazz);
	
}
