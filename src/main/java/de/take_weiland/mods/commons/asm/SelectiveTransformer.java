package de.take_weiland.mods.commons.asm;


import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.internal.SevenCommons;
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
		if (bytes != null && transforms(transformedName)) {
			try {
				ClassNode clazz = ASMUtils.getClassNode(bytes);
				if (transform(clazz, transformedName)) {
					
					System.out.println("Transforming class " + transformedName);
					
					ClassWriter writer = new ExtendedClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
					clazz.accept(writer);
					bytes = writer.toByteArray();
				}
			} catch (Exception e) {
				SevenCommons.LOGGER.severe("Exception during transformation of class " + transformedName);
				e.printStackTrace();
				Throwables.propagate(e);
			}
		}
		return bytes;
	}
	
	protected abstract boolean transforms(String className);
	
	protected abstract boolean transform(ClassNode clazz, String className);
	
}
