package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.SCReflection;
import de.take_weiland.mods.commons.fastreflect.Invoke;

/**
 * @author diesieben07
 */
public interface InternalReflector {

	public static final InternalReflector instance = SCReflection.createReflector(InternalReflector.class);

	@Invoke(method = "findLoadedClass")
	Class<?> findLoadedClass(ClassLoader classLoader, String name);

}
