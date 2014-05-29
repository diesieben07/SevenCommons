package de.take_weiland.mods.commons.asm;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.List;

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
		if (method.equals("<init>") || method.equals("<clinit>")) {
			return null;
		}
		MethodNode m = ASMUtils.findMethod(clazz, method);
		return m == null ? null : new MethodInfoASM(this, m);
	}

	@Override
	public MethodInfo getMethod(String method, String desc) {
		if (method.equals("<init>") || method.equals("<clinit>")) {
			return null;
		}
		MethodNode m = ASMUtils.findMethod(clazz, method, desc);
		return m == null ? null : new MethodInfoASM(this, m);
	}

	private List<Type[]> constructors;
	@Override
	public List<Type[]> constructorTypes() {
		if (constructors == null) {
			ImmutableList.Builder<Type[]> builder = ImmutableList.builder();
			for (MethodNode method : clazz.methods) {
				if (method.name.equals("<init>")) {
					builder.add(Type.getArgumentTypes(method.desc));
				}
			}
			constructors = builder.build();
		}
		return constructors;
	}
}
