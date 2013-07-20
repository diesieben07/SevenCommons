package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import de.take_weiland.mods.commons.util.ASMUtils;

public abstract class AppendingTransformer extends MethodTransformer {

	@Override
	protected final boolean transform(ClassNode clazz, MethodNode method) {
		method.instructions.insertBefore(ASMUtils.findLastReturn(method), getAppends(clazz, method));
		return true;
	}
	
	protected abstract InsnList getAppends(ClassNode clazz, MethodNode method);
}
