package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractASMVariable implements ASMVariable {

	@Override
	public CodePiece getAndSet(CodePiece newValue) {
		return get().append(set(newValue));
	}

	@Override
	public CodePiece setAndGet(CodePiece newValue) {
		return set(newValue).append(get());
	}

	@Override
	public final AnnotationNode getRawAnnotation(Class<? extends Annotation> ann) {
		return ASMUtils.getRawAnnotation(getterAnns(true), getterAnns(false), Type.getDescriptor(ann));
	}

	@Override
	public AnnotationNode getRawAnnotation(String internalName) {
		return ASMUtils.getRawAnnotation(getterAnns(true), getterAnns(false), ASMUtils.getDescriptor(internalName));
	}

	private Map<Class<?>, Object> annotations;

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> ann) {
		if (annotations == null) {
			annotations = new HashMap<>();
		}
		Object o = annotations.get(ann);
		if (o == null && !annotations.containsKey(ann)) {
			o = ASMUtils.makeReal(getRawAnnotation(ann), ann);
			annotations.put(ann, o);
		}
		//noinspection unchecked
		return (A) o;
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

	@Override
	public String setterName() {
		throw new UnsupportedOperationException();
	}

	abstract List<AnnotationNode> getterAnns(boolean visible);

	abstract int setterModifiers();

	abstract int getterModifiers();

}
