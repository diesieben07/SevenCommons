package de.take_weiland.mods.commons.asm;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

/**
* @author diesieben07
*/
abstract class AbstractClassInfo implements ClassInfo {

	private Set<String> supers;

	private Set<String> buildSupers() {
		Set<String> set = Sets.newHashSet();
		if (superName() != null) {
			set.add(superName());
			set.addAll(superclass().getSupers());
		}
		for (String iface : interfaces()) {
			if (set.add(iface)) {
				set.addAll(ASMUtils.getClassInfo(iface).getSupers());
			}
		}
		// use immutable set to reduce memory footprint and potentially increase performance
		return ImmutableSet.copyOf(set);
	}

	@Override
	public Set<String> getSupers() {
		return supers == null ? (supers = buildSupers()) : supers;
	}

	@Override
	public boolean isAssignableFrom(ClassInfo child) {
		// some cheap tests first
		if (child.internalName().equals("java/lang/Object")) {
			// Object is only assignable to itself
			return internalName().equals("java/lang/Object");
		}
		if (internalName().equals("java/lang/Object") // everything is assignable to Object
				|| child.internalName().equals(internalName()) // we are the same
				|| internalName().equals(child.superName()) // we are the superclass of child
				|| child.interfaces().contains(internalName())) { // we are an interface that child implements
			return true;
		}

		// if we are a class no interface can be cast to us
		if (!isInterface() && child.isInterface()) {
			return false;
		}
		// need to compute supers now
		return child.getSupers().contains(internalName());
	}

	private ClassInfo zuper;

	@Override
	public ClassInfo superclass() {
		if (zuper != null) {
			return zuper;
		}
		if (superName() == null) {
			return null;
		}
		return (zuper = ASMUtils.getClassInfo(superName()));
	}


	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof ClassInfo && internalName().equals(((ClassInfo) o).internalName());

	}

	@Override
	public int hashCode() {
		return internalName().hashCode();
	}

	@Override
	public boolean isEnum() {
		return hasModifier(ACC_ENUM) && superName().equals("java/lang/Enum");
	}

	@Override
	public boolean isAbstract() {
		return hasModifier(ACC_ABSTRACT);
	}

	@Override
	public boolean isInterface() {
		return hasModifier(ACC_INTERFACE);
	}

	private boolean hasModifier(int mod) {
		return (getModifiers() & mod) == mod;
	}
}
