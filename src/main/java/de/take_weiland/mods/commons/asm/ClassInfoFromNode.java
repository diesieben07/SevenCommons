package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

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
	public boolean isInterface() {
		return (clazz.access & ACC_INTERFACE) == ACC_INTERFACE;
	}

	@Override
	public boolean isEnum() {
		return (clazz.access & ACC_ENUM) == ACC_ENUM && clazz.superName.equals("java/lang/Enum");
	}
}