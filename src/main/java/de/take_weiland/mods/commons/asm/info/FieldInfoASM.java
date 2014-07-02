package de.take_weiland.mods.commons.asm.info;

import org.objectweb.asm.tree.FieldNode;

/**
 * @author diesieben07
 */
class FieldInfoASM extends FieldInfo {

	private final FieldNode field;

	FieldInfoASM(ClassInfo clazz, FieldNode field) {
		super(clazz);
		this.field = field;
	}

	@Override
	public String desc() {
		return field.desc;
	}

	@Override
	public String name() {
		return field.name;
	}

	@Override
	public int modifiers() {
		return field.access;
	}
}
