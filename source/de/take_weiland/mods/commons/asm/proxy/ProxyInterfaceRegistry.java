package de.take_weiland.mods.commons.asm.proxy;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.IFMLCallHook;

/**
 * A central place to register your ProxyInterfaces.<br>
 * A ProxyInterface is an Interface that will be applied to a class using ASM.<br>
 * Getters and Setters for fields can be automatically created using the {@link Getter @Getter} and {@link Setter @Setter} annotations.
 * @author diesieben07
 *
 */
public final class ProxyInterfaceRegistry {

	private ProxyInterfaceRegistry() { }
	
	private static Multimap<String, Class<?>> proxyInterfaces = HashMultimap.create();

	/**
	 * Register a new ProxyInterface<br>
	 * Given class has to be an interface and have the {@link TargetClass @TargetClass} annotation.<br>
	 * This method should most likely be called from an {@link IFMLCallHook}
	 * @param proxy the interface class to register
	 * @throws IllegalArgumentException if the class is not an interface
	 * @throws IllegalArgumentException if the class doesn't have a @TargetClass annotation
	 */
	public static void registerProxyInterface(Class<?> proxy) {
		if (!proxy.isInterface()) {
			throw new IllegalArgumentException("ProxyInterface " + proxy.getSimpleName() + " is not an interface!");
		}
		
		if (!proxy.isAnnotationPresent(TargetClass.class)) {
			throw new IllegalArgumentException("ProxyInterface " + proxy.getSimpleName() + " is missing @TargetClass annotation!");
		}
		
		String target = proxy.getAnnotation(TargetClass.class).value();
		proxyInterfaces.put(target, proxy);
	}

	static Collection<Class<?>> getProxyInterfaces(String className) {
		return proxyInterfaces.get(className);
	}

	static boolean hasProxyInterface(String className) {
		return proxyInterfaces.containsKey(className);
	}
	
}
