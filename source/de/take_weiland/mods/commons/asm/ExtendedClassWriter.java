package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.ClassWriter;
import static org.objectweb.asm.Opcodes.*;
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
			ClassNode node1;
			ClassNode node2;
			if (isAssignableFrom(type1, (node1 = ASMUtils.getClassNode(type2)))) {
				return type1;
			}
			if (isAssignableFrom(type2, (node2 = ASMUtils.getClassNode(type1)))) {
				return type2;
			}
			if ((node1.access & ACC_INTERFACE) == ACC_INTERFACE || (node2.access & ACC_INTERFACE) == ACC_INTERFACE) {
				return "java/lang/Object";
			} else {
				do {
					String superName = node1.superName;
					if (superName.equals("java/lang/Object")) {
						return "java/lang/Object";
					}
					node1 = ASMUtils.getClassNode(node1.superName);
				} while (!isAssignableFrom(node1.name, node2));
				return node1.name;
			}
		
//			throw new RuntimeException("Failed to get common superclass for " + type1 + " & " + type2, e);
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