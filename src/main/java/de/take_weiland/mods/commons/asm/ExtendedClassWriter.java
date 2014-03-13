package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.ClassWriter;

import static de.take_weiland.mods.commons.asm.ASMUtils.getClassInfo;
import static de.take_weiland.mods.commons.asm.ASMUtils.isAssignableFrom;

/**
 * A class writer that does not load classes do compute frames but instead uses bytecode-analysis
 */
public class ExtendedClassWriter extends ClassWriter {

	public ExtendedClassWriter(int flags) {
		super(flags);
	}
	
	@Override
	protected String getCommonSuperClass(String type1, String type2) {
        ClassInfo cl1 = getClassInfo(type1);
        ClassInfo cl2 = getClassInfo(type2);
        
        if (isAssignableFrom(cl1, cl2)) {
            return type1;
        }
        if (isAssignableFrom(cl2, cl1)) {
            return type2;
        }
        if (cl1.isInterface() || cl2.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                cl1 = getClassInfo(cl1.superName());
            } while (!isAssignableFrom(cl1, cl2));
            return cl1.internalName();
        }
	}

}