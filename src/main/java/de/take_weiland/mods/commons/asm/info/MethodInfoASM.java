package de.take_weiland.mods.commons.asm.info;

import org.objectweb.asm.tree.MethodNode;

/**
 * @author diesieben07
 */
class MethodInfoASM extends MethodInfo {

	private final MethodNode method;

	MethodInfoASM(ClassInfo clazz, MethodNode method) {
		super(clazz);
		this.method = method;
	}

	@Override
	public String name() {
		return method.name;
	}

	@Override
	public String desc() {
		return method.desc;
	}

	@Override
	public int modifiers() {
		return method.access;
	}

}
