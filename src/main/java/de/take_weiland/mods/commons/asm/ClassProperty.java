package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;

/**
 * Represents an ASM node that provides access to a value, usually a FieldNode or a getter/setter pair
 * @author diesieben07
 */
public interface ClassProperty {

	/**
	 * generate a new CodePiece that will load the value represented on the stack
	 * @return the CodePiece
	 */
	CodePiece getValue();

	/**
	 * generate a new CodePiece that will save a value to the field
	 * @param loadValue a CodePiece that loads the desired value onto the stack
	 * @return the CodePiece
	 * @throws java.lang.UnsupportedOperationException if {@link #isWritable()} is false
	 */
	CodePiece setValue(CodePiece loadValue);

	AnnotationNode getterAnnotation(Class<? extends Annotation> ann);

	AnnotationNode setterAnnotation(Class<? extends Annotation> ann);

	boolean hasSetterModifier(int modifier);

	boolean hasGetterModifier(int modifier);

	Type getType();

	String propertyName();

	boolean isStatic();

	/**
	 * If this <tt>ClassProperty</tt> allows value-writes.
	 * @return true if writing is allowed
	 */
	boolean isWritable();

}
