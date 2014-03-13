package de.take_weiland.mods.commons.asm;

import java.util.Collection;
import java.util.Set;

/**
 * some information about a class, obtain via {@link #getClassInfo(String)}, {@link #getClassInfo(Class)} or {@link #getClassInfo(org.objectweb.asm.tree.ClassNode)}
 */
public interface ClassInfo {

	/**
	 * a collection of internal names, representing the interfaces directly implemented by this class
	 * @return the interfaces implemented by this class
	 *
	 */
	Collection<String> interfaces();

	/**
	 * the internal name of the superclass of this class
	 * @return the superclass, or null if this ClassInfo is an interface or represents java/lang/Object
	 */
	String superName();

	/**
	 * the internal name of this class
	 * @return
	 */
	String internalName();

	/**
	 * get a ClassInfo representing the superclass of this class
	 * @return the superclass or null if this ClassInfo is an interface or represents java/lang/Object
	 */
	ClassInfo superclass();

	/**
	 * check if this class is an interface
	 * @return true if this class is an interface
	 */
	boolean isInterface();

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

}
