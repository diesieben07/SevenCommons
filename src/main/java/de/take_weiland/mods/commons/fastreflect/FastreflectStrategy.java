package de.take_weiland.mods.commons.fastreflect;

interface FastreflectStrategy {

	<T> T createAccessor(Class<T> iface);

	Class<?> defineDynClass(byte[] clazz, Class<?> context);
	
}
