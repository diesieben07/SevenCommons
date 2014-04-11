package de.take_weiland.mods.commons.asm;

import java.util.Collection;
import java.util.Set;

/**
 * some information about a class, obtain via {@link de.take_weiland.mods.commons.asm.ASMUtils#getClassInfo(String)}, {@link de.take_weiland.mods.commons.asm.ASMUtils#getClassInfo(Class)} or {@link de.take_weiland.mods.commons.asm.ASMUtils#getClassInfo(org.objectweb.asm.tree.ClassNode)}
 */
public interface ClassInfo {

	/**
	 * a collection of internal names, representing the interfaces directly implemented by this class
	 * @return the interfaces implemented by this class
	 *
	 */
	Collection<String> interfaces();

	/**
	 * get the internal name of the superclass of this class
	 * @return the superclass, or null if this ClassInfo is an interface or represents java/lang/Object
	 */
	String superName();

	/**
	 * get the internal name of this class
	 * @return the internal name
	 */
	String internalName();

	/**
	 * get a ClassInfo representing the superclass of this class
	 * @return the superclass or null if this ClassInfo is an interface or represents java/lang/Object
	 */
	ClassInfo superclass();

	/**
	 * <p>check if this class is either a superclass or superinterface of the passed in class.</p>
	 * <p>This is equivalent to calling {@link Class#isAssignableFrom(Class)} with actual class objects, except
	 * that this method does not trigger loading of any new classes.</p>
	 * @param other the class to check
	 * @return true if this class is assignable from the other
	 */
	boolean isAssignableFrom(ClassInfo other);

	/**
	 * return a set of all superclasses and superinterfaces of this class
	 * @return all superclasses and superinterfaces
	 */
	Set<String> getSupers();

	/**
	 * get all Modifiers present on this class. Equivalent to {@link Class#getModifiers()}
	 * @return the modifiers
	 */
	int getModifiers();

	/**
	 * <p>returns true if this class represents an Enum class. Equivalent to {@link Class#isEnum()}</p>
	 * @return true if this ClassInfo represents an Enum class
	 */
	boolean isEnum();

	/**
	 * <p>returns true if this class is abstract.</p>
	 * @return true if this class is abstract
	 */
	boolean isAbstract();

	/**
	 * check if this class is an interface
	 * @return true if this class is an interface
	 */
	boolean isInterface();

}
