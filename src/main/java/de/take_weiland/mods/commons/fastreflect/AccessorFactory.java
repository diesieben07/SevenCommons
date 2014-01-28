package de.take_weiland.mods.commons.fastreflect;

interface AccessorFactory {

	<T> T createAccessor(Class<T> iface);
	
}
