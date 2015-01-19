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
	 * @return a CodePiece
	 */
	CodePiece get();

	/**
	 * <p>Create a {@code CodePiece} that will set the value of this variable to the value provided by the given
	 * {@code CodePiece}.</p>
	 *
	 * @param newValue a CodePiece representing the new value
	 * @return a CodePiece
	 */
	CodePiece set(CodePiece newValue);

	/**
	 * <p>Create a {@code CodePiece} that will set the value of this variable to the given new value and leave the original
	 * value of the variable on the stack.</p>
	 * <p>This method produces code that is potentially more effective than using <code>var.get().append(var.set(newValue));</code>.</p>
	 * @param newValue a CodePiece representing the new value
	 * @return a CodePiece
	 */
	CodePiece getAndSet(CodePiece newValue);

	/**
	 * <p>Create a {@code CodePiece} that will set the value of this variable to the given new value and then leave
	 * that new value on the stack.</p>
	 * <p>This method produces code that will only execute the code in {@code newValue} once.</p>
	 * @param newValue a CodePiece representing the new value
	 * @return a CodePiece
	 */
	CodePiece setAndGet(CodePiece newValue);

	/**
	 * <p>Get an {@code AnnotationNode} for the given annotation class,
	 * if it is present on the getter / the field of this variable.</p>
	 *
	 * @param ann the annotation class
	 * @return an AnnotationNode
	 */
	AnnotationNode getRawAnnotation(Class<? extends Annotation> ann);

	<A extends Annotation> A getAnnotation(Class<A> ann);

	/**
	 * <p>Get an {@code AnnotationNode} for the given annotation class,
	 * if it is present on the getter / the field of this variable.</p>
	 *
	 * @param internalName the internal name of the annotation class
	 * @return an AnnotationNode
	 */
	AnnotationNode getRawAnnotation(String internalName);

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

	/**
	 * <p>Get the raw name of this variable. For fields this will be equivalent to {@link #name()}, for a getter/setter pair
	 * it will be the name of the getter (e.g. getFoobar).</p>
	 * @return the raw name of this variable
	 */
	String rawName();

	/**
	 * <p>Get the raw name of the setter method of this variable. If this variable is not represented by a getter/setter pair,
	 * this method will throw an exception.</p>
	 * @return the setter name
	 */
	String setterName();

	/**
	 * <p>Check if this variable has a static modifier.</p>
	 *
	 * @return true if this variable is static
	 */
	boolean isStatic();

	/**
	 * <p>Checks if this variable allows value-writes.</p>
	 * <p>Note that a true returned from this method does not mean the variable can actually be written to. A final field
	 * for example will still return true from this method, whereas a getter without a setter will not.</p>
	 *
	 * @return true if writing to this variable is allowed
	 */
	boolean isWritable();

	/**
	 * <p>Checks if this variable represents a field.</p>
	 * @return true if this variable represents a field
	 */
	boolean isField();

	/**
	 * <p>Checks if this variable represents a method.</p>
	 * @return true if this variable represents a method
	 */
	boolean isMethod();

}
