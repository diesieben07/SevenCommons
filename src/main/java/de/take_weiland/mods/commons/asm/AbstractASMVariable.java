package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.List;

abstract class AbstractASMVariable implements ASMVariable {

	@Override
	public final AnnotationNode getterAnnotation(Class<? extends Annotation> ann) {
		return ASMUtils.getAnnotation(getterAnns(true), getterAnns(false), annotationType(), ann);
	}

	@Override
	public final AnnotationNode setterAnnotation(Class<? extends Annotation> ann) {
		checkWritable();
		return ASMUtils.getAnnotation(setterAnns(true), setterAnns(false), annotationType(), ann);
	}

	@Override
	public final boolean hasSetterModifier(int modifier) {
		checkWritable();
		return (setterModifiers() & modifier) == modifier;
	}

	final void checkWritable() {
		if (!isWritable()) {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public final boolean hasGetterModifier(int modifier) {
		return (getterModifiers() & modifier) == modifier;
	}

	@Override
	public final boolean isStatic() {
		return hasGetterModifier(Opcodes.ACC_STATIC);
	}

	@Override
	public String rawName() {
		return name();
	}

	abstract List<AnnotationNode> getterAnns(boolean visible);

	abstract List<AnnotationNode> setterAnns(boolean visible);

	abstract int setterModifiers();

	abstract int getterModifiers();

	abstract ElementType annotationType();

}
