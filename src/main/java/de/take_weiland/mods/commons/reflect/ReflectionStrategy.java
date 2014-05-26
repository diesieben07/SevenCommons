package de.take_weiland.mods.commons.reflect;

interface ReflectionStrategy {

	<T> T createAccessor(Class<T> iface);

	Class<?> defineDynClass(byte[] clazz, Class<?> context);
	
}
