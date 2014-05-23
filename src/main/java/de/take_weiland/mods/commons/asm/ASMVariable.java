package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;

/**
 * Represents a kind of ASM variable. Mostly this is a local variable, a field or a getter/setter pair
 * @author diesieben07
 */
public interface ASMVariable {

	CodePiece get();

	CodePiece set(CodePiece loadValue);

	AnnotationNode getterAnnotation(Class<? extends Annotation> ann);

	AnnotationNode setterAnnotation(Class<? extends Annotation> ann);

	boolean hasSetterModifier(int modifier);

	boolean hasGetterModifier(int modifier);

	Type getType();

	String name();

	boolean isStatic();

	/**
	 * If this variable allows value-writes.
	 * @return true if writing is allowed
	 */
	boolean isWritable();

}
