package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class ExtendedClassWriter extends ClassWriter {

	public ExtendedClassWriter(int flags) {
		super(flags);
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		try {
			return super.getCommonSuperClass(type1, type2);
		} catch (RuntimeException e) {
			if (isAssignableFrom(type1, ASMUtils.getClassNode(type2))) {
				return type1;
			} else if (isAssignableFrom(type2, ASMUtils.getClassNode(type1))) {
				return type2;
			}
			throw new RuntimeException("Failed to get common superclass for " + type1 + " & " + type2, e);
		}
	}

	private static boolean isAssignableFrom(String parent, ClassNode child) {
		if (parent.equals(child.name) || parent.equals(child.superName) || child.interfaces.contains(parent)) {
			return true;
		} else if (!child.superName.equals("java/lang/Object")) {
			return isAssignableFrom(parent, ASMUtils.getClassNode(child.superName));
		} else {
			return false;
		}
	}
}