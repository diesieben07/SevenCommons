package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.fastreflect.Invoke;

/**
 * @author diesieben07
 */
public interface InternalReflector {

	@Invoke(method = "findLoadedClass")
	Class<?> findLoadedClass(ClassLoader classLoader, String name);

}
