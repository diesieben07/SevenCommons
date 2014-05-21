package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.List;

import static org.objectweb.asm.Opcodes.ALOAD;

/**
 * @author diesieben07
 */
abstract class AbstractClassProperty implements ClassProperty {

	private Type type;

	@Override
	public CodePiece set(CodePiece loadValue) {
		checkStatic();
		return makeSet(null, loadValue);
	}

	@Override
	public CodePiece set(CodePiece instance, CodePiece loadValue) {
		checkNotStatic();
		return makeSet(instance, loadValue);
	}

	@Override
	public CodePiece get() {
		checkStatic();
		return makeGet(null);
	}

	@Override
	public CodePiece get(CodePiece instance) {
		checkNotStatic();
		return makeGet(instance);
	}

	@Override
	public final CodePiece getFromThis() {
		checkNotStatic();
		return makeGet(ASMUtils.asCodePiece(new VarInsnNode(ALOAD, 0)));
	}

	@Override
	public final CodePiece setOnThis(CodePiece loadValue) {
		checkNotStatic();
		checkWritable();
		return makeSet(ASMUtils.asCodePiece(new VarInsnNode(ALOAD, 0)), loadValue);
	}

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

	final void checkNotStatic() {
		if (!isStatic()) {
			throw new UnsupportedOperationException("Expected non-static property.");
		}
	}

	final void checkStatic() {
		if (isStatic()) {
			throw new UnsupportedOperationException("Expected static property.");
		}
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
	public Type getType() {
		return type == null ? (type = makeType()) : type;
	}

	abstract Type makeType();

	abstract List<AnnotationNode> getterAnns(boolean visible);

	abstract List<AnnotationNode> setterAnns(boolean visible);

	abstract int setterModifiers();

	abstract int getterModifiers();

	abstract ElementType annotationType();

	abstract CodePiece makeGet(CodePiece instance);

	abstract CodePiece makeSet(CodePiece instance, CodePiece loadValue);
}
