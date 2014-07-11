package de.take_weiland.mods.commons.asm.info;

import de.take_weiland.mods.commons.asm.ASMUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
final class ClassInfoFromNode extends ClassInfo {

	private final ClassNode clazz;

	ClassInfoFromNode(ClassNode clazz) {
		this.clazz = checkNotNull(clazz, "ClassNode");
	}

	@Override
	public List<String> interfaces() {
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
	public boolean hasField(String name) {
		return ASMUtils.findField(clazz, name) != null;
	}

	@Override
	public FieldInfo getField(String name) {
		FieldNode field = ASMUtils.findField(clazz, name);
		return field != null ? new FieldInfoASM(this, field) : null;
	}

	@Override
	public boolean hasMethod(String name) {
		return !isHidden(name) && ASMUtils.findMethod(clazz, name) != null;
	}

	@Override
	public boolean hasMethod(String name, String desc) {
		return !isHidden(name) && ASMUtils.findMethod(clazz, name, desc) != null;
	}

	@Override
	public MethodInfo getMethod(String name) {
		if (isHidden(name)) {
			return null;
		}
		MethodNode m = ASMUtils.findMethod(clazz, name);
		return m == null ? null : new MethodInfoASM(this, m);
	}

	@Override
	public MethodInfo getMethod(String name, String desc) {
		if (isHidden(name)) {
			return null;
		}
		MethodNode m = ASMUtils.findMethod(clazz, name, desc);
		return m == null ? null : new MethodInfoASM(this, m);
	}

	@Override
	public boolean hasConstructor(String desc) {
		return ASMUtils.findMethod(clazz, "<init>", desc) != null;
	}

	@Override
	public MethodInfo getConstructor(String desc) {
		MethodNode c = ASMUtils.findMethod(clazz, "<init>", desc);
		return c == null ? null : new MethodInfoASM(this, c);
	}

	private static boolean isHidden(String name) {
		return name.equals("<init>") || name.equals("<clinit>");
	}

}
