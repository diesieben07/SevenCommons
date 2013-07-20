package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public abstract class PrependingTransformer extends MethodTransformer {

	@Override
	protected boolean transform(ClassNode clazz, MethodNode method) {
		method.instructions.insert(getPrepends(clazz, method));
		return true;
	}
	
	protected abstract InsnList getPrepends(ClassNode clazz, MethodNode method);
}
