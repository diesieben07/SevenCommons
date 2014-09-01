package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;

/**
 * <p>Represents a kind of ASM variable. Usually this is a local variable, a field or a getter/setter pair.</p>
 *
 * @author diesieben07
 * @see de.take_weiland.mods.commons.asm.ASMVariables
 */
public interface ASMVariable {

	/**
	 * <p>Create a {@code CodePiece} that will get the value of this variable and leave it on the stack.</p>
	 *
	 * @return a CodePiece that gets the value of this variable
	 */
	CodePiece get();

	/**
	 * <p>Create a {@code CodePiece} that will set the value of this variable to the value provided by the given
	 * {@code CodePiece}.</p>
	 *
	 * @param newValue a CodePiece that will leave the desired value on top of the stack
	 * @return a CodePiece that sets the value of this variable
	 */
	CodePiece set(CodePiece newValue);

	CodePiece getAndSet(CodePiece newValue);

	CodePiece setAndGet(CodePiece newValue);

	/**
	 * <p>Get an {@code AnnotationNode} for the given annotation class,
	 * if it is present on the getter / the field of this variable.</p>
	 * <p>If this variable is not represented by a getter and setter, this method and
	 * {@link #setterAnnotation(Class)} have the same functionality.</p>
	 *
	 * @param ann the annotation class to get
	 * @return an AnnotationNode
	 */
	AnnotationNode getterAnnotation(Class<? extends Annotation> ann);

	AnnotationNode getterAnnotation(String name);

	/**
	 * <p>Get an {@code AnnotationNode} for the given annotation class,
	 * if it is present on the setter / the field of this variable.</p>
	 * <p>If this variable is not represented by a getter and setter, this method and
	 * {@link #getterAnnotation(Class)} have the same functionality.</p>
	 *
	 * @param ann the annotation class to get
	 * @return an AnnotationNode
	 */
	AnnotationNode setterAnnotation(Class<? extends Annotation> ann);

	AnnotationNode setterAnnotation(String name);

	/**
	 * <p>Determine if the given modifier is present on the getter / the field of this variable.</p>
	 * <p>If this variable is not represented by a getter and setter, this method and
	 * {@link #hasSetterModifier(int)} have the same functionality.</p>
	 *
	 * @param modifier the modifier to check for (e.g. {@link java.lang.reflect.Modifier#PUBLIC}
	 * @return true if the given modifier is present
	 */
	boolean hasGetterModifier(int modifier);

	/**
	 * <p>Determine if the given modifier is present on the setter / the field of this variable.</p>
	 * <p>If this variable is not represented by a getter and setter, this method and
	 * {@link #hasGetterModifier(int)} have the same functionality.</p>
	 *
	 * @param modifier the modifier to check for (e.g. {@link java.lang.reflect.Modifier#PUBLIC}
	 * @return true if the given modifier is present
	 */
	boolean hasSetterModifier(int modifier);

	/**
	 * <p>Get the Type of this variable.</p>
	 *
	 * @return a Type
	 */
	Type getType();

	/**
	 * <p>Get the name of this variable. For fields this will be the field name, for a getter / setter pair it will
	 * be the property name (e.g. getFoobar =&gt; foobar).</p>
	 *
	 * @return the name of this variable
	 */
	String name();

	String rawName();

	/**
	 * <p>Check if this variable has a static modifier.</p>
	 *
	 * @return true if this variable is static
	 */
	boolean isStatic();

	/**
	 * <p>Checks if this variable allows value-writes.</p>
	 *
	 * @return true if writing to this variable is allowed
	 */
	boolean isWritable();

	boolean isField();

	boolean isMethod();

}
