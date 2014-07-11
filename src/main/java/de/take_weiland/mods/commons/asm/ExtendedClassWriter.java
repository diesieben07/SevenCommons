package de.take_weiland.mods.commons.asm;

import de.take_weiland.mods.commons.asm.info.ClassInfo;
import org.objectweb.asm.ClassWriter;

/**
 * A class writer that does not load classes do compute frames but instead uses bytecode-analysis
 */
public class ExtendedClassWriter extends ClassWriter {

	public ExtendedClassWriter(int flags) {
		super(flags);
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		ClassInfo cl1 = ClassInfo.of(type1);
		ClassInfo cl2 = ClassInfo.of(type2);

		if (cl1.isAssignableFrom(cl2)) {
			return type1;
		}
		if (cl2.isAssignableFrom(cl1)) {
			return type2;
		}
		if (cl1.isInterface() || cl2.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				cl1 = ClassInfo.of(cl1.superName());
			} while (!cl1.isAssignableFrom(cl2));
			return cl1.internalName();
		}
	}

}