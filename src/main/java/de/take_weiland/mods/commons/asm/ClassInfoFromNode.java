package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

/**
* @author diesieben07
*/
final class ClassInfoFromNode extends AbstractClassInfo {

	private final ClassNode clazz;

	ClassInfoFromNode(ClassNode clazz) {
		this.clazz = clazz;
	}

	@Override
	public Collection<String> interfaces() {
		return clazz.interfaces;
	}

	@Override
	public String superName() {
		return clazz.superName;
	}

	@Override
	public String internalName() {
		return clazz.name;
	}

	@Override
	public int getModifiers() {
		return clazz.access;
	}
}
