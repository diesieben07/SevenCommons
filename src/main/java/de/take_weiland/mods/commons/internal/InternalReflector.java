package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.reflect.Invoke;
import de.take_weiland.mods.commons.reflect.SCReflection;

/**
 * @author diesieben07
 */
public interface InternalReflector {

	public static final InternalReflector instance = SCReflection.createAccessor(InternalReflector.class);

	@Invoke(method = "findLoadedClass")
	Class<?> findLoadedClass(ClassLoader classLoader, String name);

}
