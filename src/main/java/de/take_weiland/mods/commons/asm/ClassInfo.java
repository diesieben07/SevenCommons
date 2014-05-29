package de.take_weiland.mods.commons.asm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.take_weiland.mods.commons.internal.InternalReflector;
import de.take_weiland.mods.commons.internal.SevenCommons;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static de.take_weiland.mods.commons.internal.SevenCommons.CLASSLOADER;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * Some information about a class, obtain via {@link ClassInfo#of(String)}, {@link ClassInfo#of(Class)} or {@link ClassInfo#of(org.objectweb.asm.tree.ClassNode)}
 * @author diesieben07
 */
public abstract class ClassInfo {

	private static LoadingCache<String, ClassInfo> cache;

	static {
		cache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(3, TimeUnit.MINUTES)
				.build(new ClassInfoLoader());
	}

	private Set<String> supers;
	private ClassInfo zuper;

	/**
	 * create a {@link de.take_weiland.mods.commons.asm.ClassInfo} representing the given Class
	 * @param clazz the Class
	 * @return a ClassInfo
	 */
	public static ClassInfo of(Class<?> clazz) {
		return new ClassInfoFromClazz(clazz);
	}

	/**
	 * create a {@link de.take_weiland.mods.commons.asm.ClassInfo} representing the given ClassNode
	 * @param clazz the ClassNode
	 * @return a ClassInfo
	 */
	public static ClassInfo of(ClassNode clazz) {
		return new ClassInfoFromNode(clazz);
	}

	public static ClassInfo of(Type type) {
		switch (type.getSort()) {
			case ARRAY:
			case OBJECT:
				// Type.getClassName incorrectly returns something like "java.lang.Object[][]" instead of "[[Ljava.lang.Object"
				// so we have to convert the internal name (which is corrent) manually
				return cache.getUnchecked(ASMUtils.binaryName(type.getInternalName()));
			case METHOD:
				throw new IllegalArgumentException("Invalid Type!");
			default:
				// primitives
				return of(type.getClassName());
		}
	}

	/**
	 * <p>create a {@link de.take_weiland.mods.commons.asm.ClassInfo} representing the given class.</p>
	 * <p>This method will not load any classes through the ClassLoader directly, but instead use the ASM library to analyze the raw class bytes.</p>
	 * @param className the class
	 * @return a ClassInfo
	 * @throws de.take_weiland.mods.commons.asm.MissingClassException if the class could not be found
	 */
	public static ClassInfo of(String className) {
		return cache.getUnchecked(ASMUtils.binaryName(className));
	}

	private static ClassInfo create(String className) {
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
		if ((clazz = InternalReflector.instance.findLoadedClass(CLASSLOADER, className)) != null) {
			return new ClassInfoFromClazz(clazz);
		// didn't find it. Try with the transformed name now
		} else if ((clazz = InternalReflector.instance.findLoadedClass(CLASSLOADER, ASMUtils.transformName(className))) != null) {
			return new ClassInfoFromClazz(clazz);
		} else {
			try {
				// the class is definitely not loaded, get it's bytes
				byte[] bytes = SevenCommons.CLASSLOADER.getClassBytes(ASMUtils.transformName(className));
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
			// we've tried everything. This class doesn't fucking exist.
			throw new MissingClassException(className, e);
		}
	}

	/**
	 * a collection of internal names, representing the interfaces directly implemented by this class
	 * @return the interfaces implemented by this class
	 *
	 */
	public abstract Collection<String> interfaces();

	/**
	 * get the internal name of the superclass of this class
	 * @return the superclass, or null if this ClassInfo is an interface or represents java/lang/Object
	 */
	public abstract String superName();

	/**
	 * get the internal name of this class
	 * @return the internal name
	 */
	public abstract String internalName();

	public ClassInfo superclass() {
		if (zuper != null) {
			return zuper;
		}
		if (superName() == null) {
			return null;
		}
		return (zuper = of(superName()));
	}

	public boolean isAssignableFrom(ClassInfo child) {
		// some cheap tests first
		if (child.internalName().equals("java/lang/Object")) {
			// Object is only assignable to itself
			return internalName().equals("java/lang/Object");
		}
		if (internalName().equals("java/lang/Object") // everything is assignable to Object
				|| child.internalName().equals(internalName()) // we are the same
				|| internalName().equals(child.superName()) // we are the superclass of child
				|| child.interfaces().contains(internalName())) { // we are an interface that child implements
			return true;
		}

		// if we are a class no interface can be cast to us
		if (!isInterface() && child.isInterface()) {
			return false;
		}
		// need to compute supers now
		return child.getSupers().contains(internalName());
	}

	private Set<String> buildSupers() {
		Set<String> set = Sets.newHashSet();
		if (superName() != null) {
			set.add(superName());
			set.addAll(superclass().getSupers());
		}
		for (String iface : interfaces()) {
			if (set.add(iface)) {
				set.addAll(of(iface).getSupers());
			}
		}
		// use immutable set to reduce memory footprint and potentially increase performance
		return ImmutableSet.copyOf(set);
	}

	public Set<String> getSupers() {
		return supers == null ? (supers = buildSupers()) : supers;
	}

	/**
	 * get all Modifiers present on this class. Equivalent to {@link Class#getModifiers()}
	 * @return the modifiers
	 */
	public abstract int modifiers();

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof ClassInfo && internalName().equals(((ClassInfo) o).internalName());

	}

	@Override
	public int hashCode() {
		return internalName().hashCode();
	}

	public boolean isEnum() {
		return hasModifier(ACC_ENUM) && superName().equals("java/lang/Enum");
	}

	public boolean isAbstract() {
		return hasModifier(ACC_ABSTRACT);
	}

	public boolean isInterface() {
		return hasModifier(ACC_INTERFACE);
	}

	public boolean isArray() {
		return getDimensions() > 0;
	}

	public abstract int getDimensions();

	public boolean hasModifier(int mod) {
		return (modifiers() & mod) == mod;
	}

	public abstract boolean hasMethod(String method);

	public abstract boolean hasMethod(String method, String desc);

	public List<Type[]> constructorTypes() {
		return constructorTypes(true);
	}

	public abstract List<Type[]> constructorTypes(boolean includePrivate);

	public abstract MethodInfo getMethod(String method);

	public abstract MethodInfo getMethod(String method, String desc);

	public boolean hasConstructor(Type... parameters) {
		for (Type[] params : constructorTypes()) {
			if (Arrays.equals(params, parameters)) {
				return true;
			}
		}
		return false;
	}

	public MethodInfo unboundMethod(String method, String desc) {
		return unboundMethod(method, desc, ACC_PUBLIC);
	}

	public MethodInfo unboundStaticMethod(String method, String desc) {
		return unboundStaticMethod(method, desc, ACC_PUBLIC | ACC_STATIC);
	}

	public MethodInfo unboundMethod(String method, String desc, int modifiers) {
		return MethodInfo.unbound(internalName(), method, desc, modifiers);
	}

	public MethodInfo unboundStaticMethod(String method, String desc, int modifiers) {
		return unboundMethod(method, desc, modifiers | ACC_STATIC);
	}

	private static class ClassInfoLoader extends CacheLoader<String, ClassInfo> {

		@Override
		public ClassInfo load(String clazz) throws MissingClassException {
			return ClassInfo.create(clazz);
		}

	}

}
