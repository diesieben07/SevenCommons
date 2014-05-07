package de.take_weiland.mods.commons.asm;

import com.google.common.collect.Collections2;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Collection;

/**
* @author diesieben07
*/
final class ClassInfoFromClazz extends AbstractClassInfo {

	private final Class<?> clazz;
	private final Collection<String> interfaces;

	ClassInfoFromClazz(Class<?> clazz) {
		this.clazz = clazz;
		interfaces = Collections2.transform(Arrays.asList(clazz.getInterfaces()), ClassToNameFunc.INSTANCE);
	}

	@Override
	public Collection<String> interfaces() {
		return interfaces;
	}

	@Override
	public String superName() {
		Class<?> s = clazz.getSuperclass();
		return s == null ? null : Type.getInternalName(s);
	}

	@Override
	public String internalName() {
		return Type.getInternalName(clazz);
	}

	@Override
	public int getModifiers() {
		return clazz.getModifiers();
	}

	@Override
	public boolean isAssignableFrom(ClassInfo child) {
		if (child instanceof ClassInfoFromClazz) {
			return this.clazz.isAssignableFrom(((ClassInfoFromClazz) child).clazz);
		} else {
			return super.isAssignableFrom(child);
		}
	}
}
