package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;

/**
* @author diesieben07
*/
final class ClassInfoFromNode extends ClassInfo {

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
	public int modifiers() {
		return clazz.access;
	}

	@Override
	public int getDimensions() {
		// we never load array classes as a ClassNode
		return 0;
	}

	@Override
	public boolean hasMethod(String method) {
		return ASMUtils.findMethod(clazz, method) != null;
	}

	@Override
	public boolean hasMethod(String method, String desc) {
		return ASMUtils.findMethod(clazz, method, desc) != null;
	}

	@Override
	public MethodInfo getMethod(String method) {
		MethodNode m = ASMUtils.findMethod(clazz, method);
		return m == null ? null : new MethodInfoASM(this, m);
	}

	@Override
	public MethodInfo getMethod(String method, String desc) {
		MethodNode m = ASMUtils.findMethod(clazz, method, desc);
		return m == null ? null : new MethodInfoASM(this, m);
	}

}
