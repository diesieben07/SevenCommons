package de.take_weiland.mods.commons;

import de.take_weiland.mods.commons.fastreflect.Fastreflect;

/**
 * @author diesieben07
 */
public final class SCReflection {

	public static <T> T createReflector(Class<T> clazz) {
		return Fastreflect.createAccessor(clazz);
	}

	/**
	 * define a temporary class from the bytes which can be garbage collected if no longer in use.
	 * @param clazz the bytes describing the class
	 * @return the defined class
	 */
	public static Class<?> defineDynamicClass(byte[] clazz) {
		return Fastreflect.defineDynamicClass(clazz);
	}

	/**
	 * Same as {@link #defineDynamicClass(byte[])} but defines the class in the given context
	 */
	public static Class<?> defineDynamicClass(byte[] clazz, Class<?> context) {
		return Fastreflect.defineDynamicClass(clazz, context);
	}

	/**
	 * get a unique name for a dynamic class
	 * @return a unique name
	 */
	public static String nextDynamicClassName() {
		return Fastreflect.nextDynamicClassName();
	}

	public static String nextDynamicClassName(Package pkg) {
		return Fastreflect.nextDynamicClassName(pkg);
	}

	public static String nextDynamicClassName(String pkg) {
		return Fastreflect.nextDynamicClassName(pkg);
	}

	public static Class<?> getCallerClass() {
		return Fastreflect.getCallerClass();
	}

	public static Class<?> getCallerClass(int level) {
		return Fastreflect.getCallerClass(level);
	}

	private SCReflection() { }

}
