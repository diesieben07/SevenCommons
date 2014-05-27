package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.List;

/**
 * Default base implementation of {@link ASMVariable}
 * @author diesieben07
 */
public abstract class AbstractASMVariable implements ASMVariable {

	@Override
	public final AnnotationNode getterAnnotation(Class<? extends Annotation> ann) {
		return ASMUtils.getAnnotationRaw(getterAnns(true), getterAnns(false), annotationType(), ann);
	}

	@Override
	public final AnnotationNode setterAnnotation(Class<? extends Annotation> ann) {
		checkWritable();
		return ASMUtils.getAnnotationRaw(setterAnns(true), setterAnns(false), annotationType(), ann);
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

	protected abstract List<AnnotationNode> getterAnns(boolean visible);

	protected abstract List<AnnotationNode> setterAnns(boolean visible);

	protected abstract int setterModifiers();

	protected abstract int getterModifiers();

	protected abstract ElementType annotationType();

}
