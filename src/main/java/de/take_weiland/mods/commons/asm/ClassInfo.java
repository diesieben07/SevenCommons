package de.take_weiland.mods.commons.asm;

import de.take_weiland.mods.commons.internal.exclude.ClassInfoUtil;
import de.take_weiland.mods.commons.internal.InternalReflector;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * <p>Some information about a class, obtain via {@link ClassInfo#of(String)}, {@link ClassInfo#of(Class)} or {@link ClassInfo#of(org.objectweb.asm.tree.ClassNode)}</p>
 * @author diesieben07
 */
public abstract class ClassInfo {

	private Set<String> supers;
	private ClassInfo zuper;

	/**
	 * <p>Create a {@code ClassInfo} representing the given class.</p>
	 * @param clazz the Class
	 * @return a ClassInfo
	 */
	public static ClassInfo of(Class<?> clazz) {
		return new ClassInfoFromClazz(clazz);
	}

	/**
	 * <p>Create a {@code ClassInfo} representing the given ClassNode.</p>
	 * @param clazz the ClassNode
	 * @return a ClassInfo
	 */
	public static ClassInfo of(ClassNode clazz) {
		return new ClassInfoFromNode(clazz);
	}

	/**
	 * <p>Create a {@code ClassInfo} representing the given Type.</p>
	 * <p>This method will try to avoid loading actual classes into the JVM, but will instead use the ASM library
	 * to analyze the raw class bytes if possible.</p>
	 * @param type a Type representing the class to load, must not be a method type
	 * @return a ClassInfo
	 * @throws de.take_weiland.mods.commons.asm.MissingClassException if the class could not be found
	 */
	public static ClassInfo of(Type type) {
		switch (type.getSort()) {
			case ARRAY:
			case OBJECT:
				// Type.getClassName incorrectly returns something like "java.lang.Object[][]" instead of "[[Ljava.lang.Object"
				// so we have to convert the internal name (which is correct) manually
				return create(ASMUtils.binaryName(type.getInternalName()));
			case METHOD:
				throw new IllegalArgumentException("Invalid Type!");
			default:
				// primitives
				return of(type.getClassName());
		}
	}

	/**
	 * <p>Create a {@code ClassInfo} representing the given class.</p>
	 * <p>This method will try to avoid loading actual classes into the JVM, but will instead use the ASM library
	 * to analyze the raw class bytes if possible.</p>
	 * @param className the internal or binary name representing the class
	 * @return a ClassInfo
	 * @throws de.take_weiland.mods.commons.asm.MissingClassException if the class could not be found
	 */
	public static ClassInfo of(String className) {
		return create(ASMUtils.binaryName(className));
	}

	static ClassInfo create(String className) {
		switch (className) {
			case "boolean":
				return of(boolean.class);
			case "byte":
				return of(byte.class);
			case "short":
				return of(short.class);
			case "int":
				return of(int.class);
			case "long":
				return of(long.class);
			case "float":
				return of(float.class);
			case "double":
				return of(double.class);
			case "char":
				return of(char.class);
			default:
				if (className.indexOf('[') >= 0) {
					// array classes should always be accessible via Class.forName
					// without loading the element-type class (Object[].class doesn't load Object.class)
					return forceLoad(className);
				} else {
					return ofObject(className);
				}
		}
	}

	private static ClassInfo ofObject(String className) {
		Class<?> clazz;
		// first, try to get the class if it's already loaded
		if ((clazz = InternalReflector.instance.findLoadedClass(Launch.classLoader, className)) != null) {
			return new ClassInfoFromClazz(clazz);
		// didn't find it. Try with the transformed name now
		} else if ((clazz = InternalReflector.instance.findLoadedClass(Launch.classLoader, ASMUtils.transformName(className))) != null) {
			return new ClassInfoFromClazz(clazz);
		} else {
			try {
				// the class is definitely not loaded, get it's bytes
				byte[] bytes = Launch.classLoader.getClassBytes(ASMUtils.transformName(className));
				// somehow we can't access the class bytes (happens for JDK classes for example)
				// we try and load the class now
				if (bytes == null) {
					return forceLoad(className);
				} else {
					// we found the bytes, lets use them
					return new ClassInfoFromNode(ASMUtils.getThinClassNode(bytes));
				}
			} catch (IOException e) {
				// something went wrong getting the class bytes. try and load it
				return forceLoad(className);
			}
		}
	}

	private static ClassInfo forceLoad(String className) {
		try {
			return of(Class.forName(className));
		} catch (Exception e) {
			throw new MissingClassException(className, e);
		}
	}

	/**
	 * <p>Get all interfaces directly implemented by this class (equivalent to {@link Class#getInterfaces()}.</p>
	 * @return the interfaces implemented by this class
	 */
	public abstract List<String> interfaces();

	/**
	 * <p>Get the internal name of the superclass of this class.</p>
	 * @return the superclass, or null if this ClassInfo is an interface or represents {@code java/lang/Object}.
	 */
	public abstract String superName();

	/**
	 * <p>Get the internal name of this class (e.g. {@code java/lang/Object}.</p>
	 * @return the internal name
	 */
	public abstract String internalName();

	/**
	 * <p>Get a {@code ClassInfo} representing the superclass of this class.</p>
	 * @return the superclass, or null if this class has no superclass (see {@link #superName()}
	 */
	public ClassInfo superclass() {
		if (zuper != null) {
			return zuper;
		}
		if (superName() == null) {
			return null;
		}
		return (zuper = of(superName()));
	}

	/**
	 * <p>Determine if the given class can be safely casted to this class (equivalent to {@link java.lang.Class#isAssignableFrom(Class)}.</p>
	 * <p>Like {@link #of(String)} this method will try to avoid loading actual classes.</p>
	 * @param child the class to check for
	 * @return true if the given class can be casted to this class
	 */
	public boolean isAssignableFrom(ClassInfo child) {
		// some cheap tests first
		String childName = child.internalName();
		String myName = internalName();

		if (childName.equals("java/lang/Object")) {
			// Object is only assignable to itself
			return myName.equals("java/lang/Object");
		}
		if (myName.equals("java/lang/Object") // everything is assignable to Object
				|| childName.equals(myName) // we are the same
				|| myName.equals(child.superName()) // we are the superclass of child
				|| child.interfaces().contains(myName)) { // we are an interface that child implements
			return true;
		}

		// if we are a class no interface can be cast to us
		if (!isInterface() && child.isInterface()) {
			return false;
		}
		// need to compute supers now
		return child.getSupers().contains(myName);
	}

	/**
	 * <p>Get all superclasses in the hierarchy chain of this class as well as all interfaces this class
	 * implements directly or indirectly.</p>
	 * <p>In other words return all classes that this class can be safely casted to.</p>
	 * @return an immutable Set containing all superclasses and interfaces
	 */
	public Set<String> getSupers() {
		return supers == null ? (supers = ClassInfoUtil.getSupers(this)) : supers;
	}

	/**
	 * <p>Get the number of dimensions of this array class, or 0 if this ClassInfo does not represent an
	 * array class.</p>
	 * @return the number of dimensions
	 */
	public abstract int getDimensions();

	/**
	 * <p>Determine if this ClassInfo represents an array class (equivalent to {@link Class#isArray()}</p>
	 * @return true if this ClassInfo represents an array class
	 */
	public boolean isArray() {
		return getDimensions() > 0;
	}

	/**
	 * <p>Get all Modifiers present on this class (equivalent to {@link Class#getModifiers()}).</p>
	 * @return the modifiers
	 */
	public abstract int modifiers();

	/**
	 * <p>Determine if this ClassInfo represents an enum class (equivalent to {@link Class#isEnum()}.</p>
	 * <p>Note: Like the JDK method this method will return false for the classes generated for specialized enum constants.</p>
	 * @return true if this ClassInfo represents an enum class
	 */
	public boolean isEnum() {
		return hasModifier(ACC_ENUM) && superName().equals("java/lang/Enum");
	}

	/**
	 * <p>Determine if this ClassInfo represents an abstract class.</p>
	 * @return true if this ClassInfo represents an abstract class
	 */
	public boolean isAbstract() {
		return hasModifier(ACC_ABSTRACT);
	}

	/**
	 * <p>Determine if this ClassInfo represents an interface.</p>
	 * @return true if this ClassInfo represents an interface
	 */
	public boolean isInterface() {
		return hasModifier(ACC_INTERFACE);
	}

	/**
	 * <p>Determine if the given modifier is set on the class represented by this ClassInfo.</p>
	 * @param mod the modifier to check
	 * @return true if the given modifier is set
	 */
	public boolean hasModifier(int mod) {
		return (modifiers() & mod) == mod;
	}

	/**
	 * <p>Determine if a method with the given name is present in the class represented by this ClassInfo.</p>
	 * @param name the method name to check for
	 * @return true if this class a method with the given name
	 */
	public abstract boolean hasMethod(String name);

	/**
	 * <p>Determine if a method with the given name and descriptor is present in the class represented by this ClassInfo.</p>
	 * @param name the method name to check for
	 * @param desc the method descriptor to check for
	 * @return true if this class a method with the given name and descriptor
	 */
	public abstract boolean hasMethod(String name, String desc);

	/**
	 * <p>Get a {@link de.take_weiland.mods.commons.asm.MethodInfo} that represents the first method in this
	 * class that has the given name.</p>
	 * @param name the method name
	 * @return a MethodInfo
	 */
	public abstract MethodInfo getMethod(String name);

	/**
	 * <p>Get a {@link de.take_weiland.mods.commons.asm.MethodInfo} that represents the method with the given name and signature
	 * in this class.</p>
	 * @param name the method name
	 * @param desc the method descriptor
	 * @return a MethodInfo
	 */
	public abstract MethodInfo getMethod(String name, String desc);

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof ClassInfo && internalName().equals(((ClassInfo) o).internalName());

	}

	@Override
	public int hashCode() {
		return internalName().hashCode();
	}

}
